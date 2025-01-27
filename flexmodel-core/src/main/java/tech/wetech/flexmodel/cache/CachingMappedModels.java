package tech.wetech.flexmodel.cache;

import tech.wetech.flexmodel.AbstractSessionContext;
import tech.wetech.flexmodel.MappedModels;
import tech.wetech.flexmodel.TypeWrapper;

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
  public List<TypeWrapper> sync(AbstractSessionContext context) {
    cache.invalidateAll();
    return delegate.sync(context);
  }

  @Override
  public List<TypeWrapper> sync(AbstractSessionContext sqlContext, Set<String> includes) {
    cache.invalidateAll();
    return delegate.sync(sqlContext, includes);
  }

  @Override
  public List<TypeWrapper> lookup(String schemaName) {
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
  public void persist(String schemaName, TypeWrapper wrapper) {
    cache.put(schemaName + ":" + wrapper.getName(), wrapper);
    delegate.persist(schemaName, wrapper);
  }

  @Override
  public TypeWrapper getModel(String schemaName, String modelName) {
    return (TypeWrapper) cache.retrieve(schemaName + ":" + modelName, () -> delegate.getModel(schemaName, modelName));
  }

  public Cache getCache() {
    return cache;
  }
}
