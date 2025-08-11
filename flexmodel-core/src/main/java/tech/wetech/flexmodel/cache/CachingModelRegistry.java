package tech.wetech.flexmodel.cache;

import tech.wetech.flexmodel.ModelRegistry;
import tech.wetech.flexmodel.model.SchemaObject;
import tech.wetech.flexmodel.session.AbstractSessionContext;

import java.util.List;
import java.util.Set;

/**
 * @author cjbi
 */
public class CachingModelRegistry implements ModelRegistry {

  private final ModelRegistry delegate;
  private final Cache cache;

  public CachingModelRegistry(ModelRegistry delegate, Cache cache) {
    this.delegate = delegate;
    this.cache = cache;
  }

  @Override
  public List<SchemaObject> loadFromDataSource(AbstractSessionContext sessionContext) {
    cache.invalidateAll();
    return delegate.loadFromDataSource(sessionContext);
  }

  @Override
  public List<SchemaObject> loadFromDataSource(AbstractSessionContext sessionContext, Set<String> includes) {
    cache.invalidateAll();
    return delegate.loadFromDataSource(sessionContext, includes);
  }

  @Override
  public List<SchemaObject> listRegistered(String schemaName) {
    return delegate.listRegistered(schemaName);
  }

  @Override
  public void unregisterAll(String schemaName) {
    cache.invalidateAll();
    delegate.unregisterAll(schemaName);
  }

  @Override
  public void unregisterAll(String schemaName, String modelName) {
    cache.invalidate(schemaName + ":" + modelName);
    delegate.unregisterAll(schemaName, modelName);
  }

  @Override
  public void register(String schemaName, SchemaObject object) {
    cache.put(schemaName + ":" + object.getName(), object);
    delegate.register(schemaName, object);
  }

  @Override
  public SchemaObject getRegistered(String schemaName, String modelName) {
    return (SchemaObject) cache.retrieve(schemaName + ":" + modelName, () -> delegate.getRegistered(schemaName, modelName));
  }

  public Cache getCache() {
    return cache;
  }
}
