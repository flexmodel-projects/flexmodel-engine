package tech.wetech.flexmodel.core.cache;

import java.util.function.Supplier;

/**
 * @author cjbi
 */
public interface Cache {

  Object get(String key);

  Object retrieve(String key, Supplier<Object> supplier);

  void put(String key, Object value);

  void invalidate(String key);

  void invalidateAll();

}
