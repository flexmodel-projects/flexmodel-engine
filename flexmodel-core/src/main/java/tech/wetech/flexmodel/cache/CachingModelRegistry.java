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
  public List<SchemaObject> loadFromDatabase(AbstractSessionContext context) {
    cache.invalidateAll();
    return delegate.loadFromDatabase(context);
  }

  @Override
  public List<SchemaObject> loadFromDatabase(AbstractSessionContext sqlContext, Set<String> includes) {
    cache.invalidateAll();
    return delegate.loadFromDatabase(sqlContext, includes);
  }

  @Override
  public List<SchemaObject> getAllRegistered(String schemaName) {
    return delegate.getAllRegistered(schemaName);
  }

  @Override
  public void unregister(String schemaName) {
    cache.invalidateAll();
    delegate.unregister(schemaName);
  }

  @Override
  public void unregister(String schemaName, String modelName) {
    cache.invalidate(schemaName + ":" + modelName);
    delegate.unregister(schemaName, modelName);
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
