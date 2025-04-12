package tech.wetech.flexmodel.reflect;

import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.dsl.Expressions;
import tech.wetech.flexmodel.mapping.TypeHandler;

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

  private final String modelName;
  private final AbstractSessionContext sessionContext;
  private final Map<String, Object> dataMap;
  private final Map<Method, Boolean> loadCache = new HashMap<>();
  private final Logger log = LoggerFactory.getLogger(LazyLoadInterceptor.class);

  public LazyLoadInterceptor(String modelName, Map<String, Object> dataMap, AbstractSessionContext sessionContext) {
    this.modelName = modelName;
    this.dataMap = dataMap;
    this.sessionContext = sessionContext;
  }

  /**
   * sqlite 不支持类型自动转换，类型不一致会导致查不到数据，所以这里进行类型转换
   * @param modelName
   * @param fieldName
   * @param value
   * @return
   */
  private Object castValueType(String modelName, String fieldName, Object value) {
    Entity entity = (Entity) sessionContext.getModel(modelName);
    TypedField<?, ?> field = entity.getField(fieldName);
    if (field != null) {
      TypeHandler<?> typeHandler = sessionContext.getTypeHandlerMap().get(field.getType());
      if (typeHandler != null) {
        return typeHandler.convertParameter(field, value);
      }
    }
    return value;
  }

  @RuntimeType
  public Object intercept(@This Object proxy, @Origin Class<?> clazz, @Origin Method method, @SuperCall Callable<?> superCall) throws Throwable {
    try {
      boolean loaded = loadCache.getOrDefault(method, false);
      if (!loaded) {
        Entity entity = (Entity) sessionContext.getModel(modelName);
        String fieldName = ReflectionUtils.getFieldNameFromGetter(method);
        TypedField<?, ?> field = entity.getField(fieldName);
        if (field instanceof RelationField relationField) {
          log.debug("intercept: {}", method.getName());
          Object localValue = dataMap.get(relationField.getLocalField());
          if (relationField.isMultiple()) {
            if (localValue == null) {
              return List.of();
            }
            try (Session session = sessionContext.getFactory().createSession(sessionContext.getSchemaName())) {
              ParameterizedType returnType = (ParameterizedType) method.getGenericReturnType();
              Class<?> returnGenericType = (Class<?>) returnType.getActualTypeArguments()[0];
              localValue = castValueType(relationField.getFrom(), relationField.getForeignField(), localValue);
              List<?> list = session.find(relationField.getFrom(), Expressions.field(relationField.getForeignField()).eq(localValue), returnGenericType);
              invokeSetter(proxy, list, clazz, fieldName, method.getReturnType());
              return list;
            }
          } else {
            if (localValue == null) {
              return null;
            }
            try (Session session = sessionContext.getFactory().createSession(sessionContext.getSchemaName())) {
              localValue = castValueType(relationField.getFrom(), relationField.getForeignField(), localValue);
              List<?> list = session.find(relationField.getFrom(), Expressions.field(relationField.getForeignField()).eq(localValue), method.getReturnType());
              if (list.isEmpty()) {
                return null;
              }
              Object result = list.getFirst();
              invokeSetter(proxy, result, clazz, fieldName, method.getReturnType());
              return result;
            }
          }
        }
      }
      return superCall.call();
    } finally {
      loadCache.put(method, true);
    }

  }

  public void invokeSetter(Object proxy, Object value, Class<?> clazz, String fieldName, Class<?> parameterType) {
    try {
      Method setter = clazz.getMethod("set" + ReflectionUtils.toUpperCamelCase(fieldName), parameterType);
      setter.invoke(proxy, value);
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }


}
