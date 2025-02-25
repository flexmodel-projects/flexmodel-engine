package tech.wetech.flexmodel.lazy;

import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.dsl.Expressions;

import java.beans.Introspector;
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

  private static final int MAX_STACK_DEPTH = 1000;

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

  @RuntimeType
  public Object intercept(@This Object proxy, @Origin Class<?> clazz, @Origin Method method, @SuperCall Callable<?> superCall) throws Throwable {
    try {
      StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
      if (stackTrace.length > MAX_STACK_DEPTH) {
        // 设置最大栈深度， 避免死循环
        log.warn("Stack depth exceeds the maximum allowed depth of {}", MAX_STACK_DEPTH);
        return superCall.call();
      }
      boolean loaded = loadCache.getOrDefault(method, false);
      if (!loaded) {
        Entity entity = (Entity) sessionContext.getModel(modelName);
        String fieldName = getFieldNameFromGetter(method);
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
              List<?> list = session.find(relationField.getFrom(), Expressions.field(relationField.getForeignField()).eq(localValue), returnGenericType);
              invokeSetter(proxy, list, clazz, fieldName, method.getReturnType());
              return list;
            }
          } else {
            try (Session session = sessionContext.getFactory().createSession(sessionContext.getSchemaName())) {
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
      Method setter = clazz.getMethod("set" + toUpperCamelCase(fieldName), parameterType);
      setter.invoke(proxy, value);
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  private String toUpperCamelCase(String str) {
    char[] cs = str.toCharArray();
    cs[0] -= 32;
    return String.valueOf(cs);
  }

  public String getFieldNameFromGetter(Method method) {
    String attr;
    if (method.getName().startsWith("get")) {
      attr = method.getName().substring(3);
    } else {
      attr = method.getName().substring(2);
    }
    return Introspector.decapitalize(attr);
  }

}
