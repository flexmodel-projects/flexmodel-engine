package tech.wetech.flexmodel.core.cache;

import tech.wetech.flexmodel.core.ModelRepository;
import tech.wetech.flexmodel.core.model.SchemaObject;
import tech.wetech.flexmodel.core.session.AbstractSessionContext;

import java.util.List;
import java.util.Set;

/**
 * @author cjbi
 */
public class CachingModelRepository implements ModelRepository {

  private final ModelRepository delegate;
  private final Cache cache;

  public CachingModelRepository(ModelRepository delegate, Cache cache) {
    this.delegate = delegate;
    this.cache = cache;
  }

  @Override
  public List<SchemaObject> syncFromDatabase(AbstractSessionContext context) {
    cache.invalidateAll();
    return delegate.syncFromDatabase(context);
  }

  @Override
  public List<SchemaObject> syncFromDatabase(AbstractSessionContext sqlContext, Set<String> includes) {
    cache.invalidateAll();
    return delegate.syncFromDatabase(sqlContext, includes);
  }

  @Override
  public List<SchemaObject> findAll(String schemaName) {
    return delegate.findAll(schemaName);
  }

  @Override
  public void deleteAll(String schemaName) {
    cache.invalidateAll();
    delegate.deleteAll(schemaName);
  }

  @Override
  public void delete(String schemaName, String modelName) {
    cache.invalidate(schemaName + ":" + modelName);
    delegate.delete(schemaName, modelName);
  }

  @Override
  public void save(String schemaName, SchemaObject object) {
    cache.put(schemaName + ":" + object.getName(), object);
    delegate.save(schemaName, object);
  }

  @Override
  public SchemaObject find(String schemaName, String modelName) {
    return (SchemaObject) cache.retrieve(schemaName + ":" + modelName, () -> delegate.find(schemaName, modelName));
  }

  public Cache getCache() {
    return cache;
  }
}
