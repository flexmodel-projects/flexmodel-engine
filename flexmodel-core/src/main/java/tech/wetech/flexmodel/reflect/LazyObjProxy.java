package tech.wetech.flexmodel.reflect;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.JsonUtils;
import tech.wetech.flexmodel.model.EntityDefinition;
import tech.wetech.flexmodel.model.field.RelationField;
import tech.wetech.flexmodel.session.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
public class LazyObjProxy {

  private static final Logger log = LoggerFactory.getLogger(LazyObjProxy.class);

  @SuppressWarnings("unchecked")
  public static <T> T createProxy(T obj, String modelName, Session session) {
    try {
      EntityDefinition entity = (EntityDefinition) session.schema().getModel(modelName);
      Class<?> subClazz = new ByteBuddy()
        .subclass(obj.getClass())
        .implement(ProxyInterface.class)
        .method(ElementMatchers.namedOneOf(getLazyMethods(entity)))
        .intercept(MethodDelegation.to(new LazyLoadInterceptor(modelName,
          JsonUtils.convertValue(obj, Map.class), session))) // 委托给 LazyLoadInterceptor
        .method(ElementMatchers.named("entityInfo"))
        .intercept(FixedValue.value(session.schema().getModel(modelName)))
        .method(ElementMatchers.named("originClass"))
        .intercept(FixedValue.value(obj.getClass()))
        .make()
        .load(obj.getClass().getClassLoader())
        .getLoaded();

      return (T) JsonUtils.convertValue(obj, subClazz);
    } catch (Throwable e) {
      log.trace("Failed to create lazy class, message: {}", e.toString());
      return obj;
//      throw new RuntimeException("Failed to create lazy user proxy", e);
    }
  }

  public static <T> List<T> createProxyList(List<T> list, String modelName, Session session) {
    List<T> result = new ArrayList<>();
    for (T o : list) {
      result.add(createProxy(o, modelName, session));
    }
    return result;
  }

  private static String[] getLazyMethods(EntityDefinition entity) {
    List<String> methodNames = new ArrayList<>();
    entity.getFields().forEach(field -> {
      if (field instanceof RelationField) {
        methodNames.add("get" + ReflectionUtils.toUpperCamelCase(field.getName()));
      }
    });
    return methodNames.toArray(new String[]{});
  }


}
