package tech.wetech.flexmodel;

import tech.wetech.flexmodel.dsl.Expressions;
import tech.wetech.flexmodel.dsl.Predicate;
import tech.wetech.flexmodel.reflect.LazyObjProxy;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author cjbi
 */
public class AbstractDataOperationsDecorator implements DataOperations {

  protected final AbstractSessionContext sessionContext;
  protected final DataOperations delegate;

  public AbstractDataOperationsDecorator(AbstractSessionContext sessionContext, DataOperations delegate) {
    this.sessionContext = sessionContext;
    this.delegate = delegate;
  }

  @Override
  public int insert(String modelName, Object record, Consumer<Object> idConsumer) {
    AtomicReference<Object> atomicId = new AtomicReference<>();
    int rows = delegate.insert(modelName, record, atomicId::set);
    Object id = atomicId.get();
    idConsumer.accept(id);
    return rows;
  }

  @Override
  public <T> T findById(String modelName, Object id, Class<T> resultType, boolean nestedQuery) {
    return LazyObjProxy.createProxy(delegate.findById(modelName, id, resultType, nestedQuery), modelName, sessionContext);
  }

  @Override
  public <T> List<T> find(String modelName, Query query, Class<T> resultType) {
    return LazyObjProxy.createProxyList(delegate.find(modelName, query, resultType), modelName, sessionContext);
  }

  @Override
  public <T> List<T> findByNativeQuery(String statement, Object params, Class<T> resultType) {
    return delegate.findByNativeQuery(statement, params, resultType);
  }

  @Override
  public <T> List<T> findByNativeQueryModel(String modelName, Object params, Class<T> resultType) {
    return delegate.findByNativeQueryModel(modelName, params, resultType);
  }

  @Override
  public long count(String modelName, Query query) {
    return delegate.count(modelName, query);
  }

  @Override
  public int updateById(String modelName, Object record, Object id) {
    return delegate.updateById(modelName, record, id);
  }

  @Override
  public int update(String modelName, Object record, String filter) {
    return delegate.update(modelName, record, filter);
  }

  @Override
  public int deleteById(String modelName, Object id) {
    try {
      Entity entity = (Entity) sessionContext.getModel(modelName);
      Supplier<Map<String, Object>> sp = () -> delegate.findById(modelName, id);
      entity.getFields().parallelStream()
        .forEach(field -> {
          if (field instanceof RelationField relationField && relationField.isCascadeDelete()) {
            Map<String, Object> data = sp.get();
            if (data != null) {
              Object localVal = data.get(relationField.getLocalField());
              if (localVal != null) {
                Predicate expr = Expressions.field(relationField.getForeignField()).eq(localVal);
                delete(relationField.getFrom(), expr);
              }
            }
          }
        });
    } catch (Exception e) {
      e.printStackTrace();
    }
    return delegate.deleteById(modelName, id);
  }

  @Override
  public int delete(String modelName, String filter) {
    try {
      Supplier<List<Map<String, Object>>> sp = () -> delegate.find(modelName, q -> q.setFilter(filter));
      Entity entity = (Entity) sessionContext.getModel(modelName);
      entity.getFields().parallelStream()
        .forEach(field -> {
          if (field instanceof RelationField relationField && relationField.isCascadeDelete()) {
            List<Map<String, Object>> list = sp.get();
            for (Map<String, Object> data : list) {
              Object localVal = data.get(relationField.getLocalField());
              if (localVal != null) {
                Predicate expr = Expressions.field(relationField.getForeignField()).eq(localVal);
                delete(relationField.getFrom(), expr);
              }
            }
          }
        });
    } catch (Exception e) {
      e.printStackTrace();
    }
    return delegate.delete(modelName, filter);
  }

  @Override
  public int deleteAll(String modelName) {
    try {
      Entity entity = (Entity) sessionContext.getModel(modelName);
      entity.getFields().parallelStream()
        .forEach(field -> {
          if (field instanceof RelationField relationField && relationField.isCascadeDelete()) {
            deleteAll(relationField.getFrom());
          }
        });
    } catch (Exception e) {
      e.printStackTrace();
    }
    return delegate.deleteAll(modelName);
  }

}
