package tech.wetech.flexmodel.cache;

import tech.wetech.flexmodel.AbstractSessionContext;
import tech.wetech.flexmodel.MappedModels;
import tech.wetech.flexmodel.Model;

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
  public List<Model> sync(AbstractSessionContext context) {
    cache.invalidateAll();
    return delegate.sync(context);
  }

  @Override
  public List<Model> sync(AbstractSessionContext sqlContext, Set<String> includes) {
    cache.invalidateAll();
    return delegate.sync(sqlContext, includes);
  }

  @Override
  public List<Model> lookup(String schemaName) {
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
  public void persist(String schemaName, Model model) {
    cache.put(schemaName + ":" + model.getName(), model);
    delegate.persist(schemaName, model);
  }

  @Override
  public Model getModel(String schemaName, String modelName) {
    return (Model) cache.retrieve(schemaName + ":" + modelName, () -> delegate.getModel(schemaName, modelName));
  }

  public Cache getCache() {
    return cache;
  }
}
