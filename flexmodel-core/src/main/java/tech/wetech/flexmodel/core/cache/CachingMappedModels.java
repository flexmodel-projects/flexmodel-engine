package tech.wetech.flexmodel.core.cache;

import tech.wetech.flexmodel.core.MappedModels;
import tech.wetech.flexmodel.core.model.SchemaObject;
import tech.wetech.flexmodel.core.session.AbstractSessionContext;

import java.util.List;
import java.util.Set;

/**
 * @author cjbi
 */
public class CachingMappedModels implements MappedModels {

  private final MappedModels delegate;
  private final Cache cache;

  public CachingMappedModels(MappedModels delegate, Cache cache) {
    this.delegate = delegate;
    this.cache = cache;
  }

  @Override
  public List<SchemaObject> sync(AbstractSessionContext context) {
    cache.invalidateAll();
    return delegate.sync(context);
  }

  @Override
  public List<SchemaObject> sync(AbstractSessionContext sqlContext, Set<String> includes) {
    cache.invalidateAll();
    return delegate.sync(sqlContext, includes);
  }

  @Override
  public List<SchemaObject> lookup(String schemaName) {
    return delegate.lookup(schemaName);
  }

  @Override
  public void removeAll(String schemaName) {
    cache.invalidateAll();
    delegate.removeAll(schemaName);
  }

  @Override
  public void remove(String schemaName, String modelName) {
    cache.invalidate(schemaName + ":" + modelName);
    delegate.remove(schemaName, modelName);
  }

  @Override
  public void persist(String schemaName, SchemaObject object) {
    cache.put(schemaName + ":" + object.getName(), object);
    delegate.persist(schemaName, object);
  }

  @Override
  public SchemaObject getModel(String schemaName, String modelName) {
    return (SchemaObject) cache.retrieve(schemaName + ":" + modelName, () -> delegate.getModel(schemaName, modelName));
  }

  public Cache getCache() {
    return cache;
  }
}
