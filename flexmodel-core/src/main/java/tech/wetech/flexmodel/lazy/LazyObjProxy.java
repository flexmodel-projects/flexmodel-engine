package tech.wetech.flexmodel.lazy;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.AbstractSessionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
public class LazyObjProxy {

  private static final Logger log = LoggerFactory.getLogger(LazyObjProxy.class);

  @SuppressWarnings("unchecked")
  public static <T> T createProxy(T obj, String modelName, AbstractSessionContext sessionContext) {
    try {
      Class<?> subClazz = new ByteBuddy()
        .subclass(obj.getClass())
        .implement(ProxyInterface.class)
        .method(ElementMatchers.isGetter())
        .intercept(MethodDelegation.to(new LazyLoadInterceptor(modelName,
          sessionContext.getJsonObjectConverter().convertValue(obj, Map.class), sessionContext))) // 委托给 LazyLoadInterceptor
        .make()
        .load(obj.getClass().getClassLoader())
        .getLoaded();
      return (T) sessionContext.getJsonObjectConverter().convertValue(obj, subClazz);
    } catch (Throwable e) {
      log.warn("Failed to create lazy class, message: {}", e.toString());
      return obj;
//      throw new RuntimeException("Failed to create lazy user proxy", e);
    }
  }

  public static <T> List<T> createProxyList(List<T> list, String modelName, AbstractSessionContext sessionContext) {
    List<T> result = new ArrayList<>();
    for (T o : list) {
      result.add(createProxy(o, modelName, sessionContext));
    }
    return result;
  }


}
