package tech.wetech.flexmodel.reflect;

import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.model.EntityDefinition;
import tech.wetech.flexmodel.model.field.RelationField;
import tech.wetech.flexmodel.model.field.TypedField;
import tech.wetech.flexmodel.query.Expressions;
import tech.wetech.flexmodel.session.AbstractSessionContext;
import tech.wetech.flexmodel.session.Session;
import tech.wetech.flexmodel.type.TypeHandler;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author cjbi
 */
public class LazyLoadInterceptor {

  private static final ThreadLocal<Map<String, Object>> loadCache = ThreadLocal.withInitial(HashMap::new);
  private final Logger log = LoggerFactory.getLogger(LazyLoadInterceptor.class);
  private final String modelName;
  private final AbstractSessionContext sessionContext;
  private final Map<String, Object> dataMap;

  public LazyLoadInterceptor(String modelName, Map<String, Object> dataMap, AbstractSessionContext sessionContext) {
    this.modelName = modelName;
    this.dataMap = dataMap;
    this.sessionContext = sessionContext;
  }

  public static void clear() {
    loadCache.get().clear();
  }

  /**
   * sqlite 不支持类型自动转换，类型不一致会导致查不到数据，所以这里进行类型转换
   *
   * @param modelName
   * @param fieldName
   * @param value
   * @return
   */
  private Object castValueType(String modelName, String fieldName, Object value) {
    EntityDefinition entity = (EntityDefinition) sessionContext.getModelDefinition(modelName);
    TypedField<?, ?> field = entity.getField(fieldName);
    if (field != null) {
      TypeHandler<?> typeHandler = sessionContext.getTypeHandlerMap().get(field.getType());
      if (typeHandler != null) {
        return typeHandler.convertParameter(field, value);
      }
    }
    return value;
  }

  /**
   * 将下划线命名转换为小驼峰命名
   *
   * @param str 下划线格式的字符串
   * @return 小驼峰格式的字符串
   */
  public static String underscoreToCamelCase(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }

    StringBuilder result = new StringBuilder();
    boolean capitalizeNext = false;

    for (char c : str.toCharArray()) {
      if (c == '_') {
        capitalizeNext = true;
      } else {
        if (capitalizeNext) {
          result.append(Character.toUpperCase(c));
          capitalizeNext = false;
        } else {
          result.append(Character.toLowerCase(c));
        }
      }
    }

    return result.toString();
  }

  @RuntimeType
  public Object intercept(@This Object proxy, @Origin Class<?> clazz, @Origin Method method, @SuperCall Callable<?> superCall) throws Throwable {
    Session session = sessionContext.getSession();
    if (session.isClosed()) {
      return superCall.call();
    }
    EntityDefinition entity = (EntityDefinition) sessionContext.getModelDefinition(modelName);
    String fieldName = ReflectionUtils.getFieldNameFromGetter(method);
    TypedField<?, ?> field = entity.getField(fieldName);
    if (field instanceof RelationField relationField) {
      return handleRelationField(proxy, clazz, method, superCall, fieldName, relationField);
    }
    return superCall.call();
  }

  private Object handleRelationField(Object proxy,
                                     Class<?> clazz,
                                     Method method,
                                     Callable<?> superCall,
                                     String fieldName,
                                     RelationField relationField) throws Exception {
    Object id = resolveRelationIdentifier(relationField);
    if (id == null) {
      return superCall.call();
    }
    String cacheKey = buildCacheKey(fieldName, id);
    Object cached = loadCache.get().get(cacheKey);
    if (cached != null) {
      applyLoadedValue(proxy, clazz, fieldName, method.getReturnType(), cached);
      return cached;
    }

    RelationLoadResult loadResult = loadRelationValue(relationField, method, id);
    if (!loadResult.loaded()) {
      return superCall.call();
    }

    Object loadedValue = loadResult.value();
    applyLoadedValue(proxy, clazz, fieldName, method.getReturnType(), loadedValue);
    loadCache.get().putIfAbsent(cacheKey, loadedValue);
    return loadedValue;
  }

  private RelationLoadResult loadRelationValue(RelationField relationField, Method method, Object identifier) {
    Object convertedIdentifier = castValueType(relationField.getFrom(), relationField.getForeignField(), identifier);
    if (relationField.isMultiple()) {
      Class<?> elementType = resolveCollectionElementType(method);
      List<?> list = sessionContext.getSession().data()
        .find(relationField.getFrom(), Expressions.field(relationField.getForeignField()).eq(convertedIdentifier), elementType, false);
      return RelationLoadResult.loaded(list);
    }

    List<?> result = sessionContext.getSession().data()
      .find(relationField.getFrom(), Expressions.field(relationField.getForeignField()).eq(convertedIdentifier), method.getReturnType(), false);
    if (result.isEmpty()) {
      return RelationLoadResult.notLoaded();
    }
    return RelationLoadResult.loaded(result.getFirst());
  }

  private Object resolveRelationIdentifier(RelationField relationField) {
    Object id = dataMap.get(relationField.getLocalField());
    if (id == null) {
      id = dataMap.get(underscoreToCamelCase(relationField.getLocalField()));
    }
    return id;
  }

  private String buildCacheKey(String fieldName, Object id) {
    return modelName + ":" + fieldName + ":" + id;
  }

  private void applyLoadedValue(Object proxy, Class<?> clazz, String fieldName, Class<?> parameterType, Object value) {
    invokeSetter(proxy, value, clazz, fieldName, parameterType);
  }

  private Class<?> resolveCollectionElementType(Method method) {
    if (method.getGenericReturnType() instanceof ParameterizedType parameterizedType) {
      if (parameterizedType.getActualTypeArguments().length == 1 && parameterizedType.getActualTypeArguments()[0] instanceof Class<?> elementType) {
        return elementType;
      }
    }
    throw new IllegalStateException("Unable to resolve collection element type for method: " + method);
  }

  public void invokeSetter(Object proxy, Object value, Class<?> clazz, String fieldName, Class<?> parameterType) {
    try {
      Method setter = clazz.getMethod("set" + ReflectionUtils.toUpperCamelCase(fieldName), parameterType);
      setter.invoke(proxy, value);
    } catch (Throwable e) {
      log.warn("Failed to invoke setter for field '{}.{}'", clazz.getName(), fieldName, e);
    }
  }

  private record RelationLoadResult(boolean loaded, Object value) {
      private static final RelationLoadResult NOT_LOADED = new RelationLoadResult(false, null);

    static RelationLoadResult loaded(Object value) {
        return new RelationLoadResult(true, value);
      }

      static RelationLoadResult notLoaded() {
        return NOT_LOADED;
      }
    }

}
