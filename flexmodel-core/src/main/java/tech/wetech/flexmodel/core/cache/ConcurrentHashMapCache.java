package tech.wetech.flexmodel.core.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @author cjbi
 */
public class ConcurrentHashMapCache implements Cache {

  private final Map<String, Object> store = new ConcurrentHashMap<>();

  @Override
  public Object get(String key) {
    return store.get(key);
  }

  @Override
  public Object retrieve(String key, Supplier<Object> supplier) {
    Object value = get(key);
    if (value == null) {
      value = supplier.get();
      if (value != null) {
        put(key, value);
      }
    }
    return value;
  }

  @Override
  public void put(String key, Object value) {
    store.put(key, value);
  }

  @Override
  public void invalidate(String key) {
    store.remove(key);
  }

  @Override
  public void invalidateAll() {
    store.clear();
  }
}
