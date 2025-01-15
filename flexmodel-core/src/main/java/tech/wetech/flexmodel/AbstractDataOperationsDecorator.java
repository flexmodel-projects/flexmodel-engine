package tech.wetech.flexmodel;

import tech.wetech.flexmodel.graph.JoinGraphNode;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

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
  public void associate(JoinGraphNode joinGraphNode, Map<String, Object> data) {
    delegate.associate(joinGraphNode, data);
  }

  @Override
  public int insert(String modelName, Map<String, Object> record, Consumer<Object> idConsumer) {
    AtomicReference<Object> atomicId = new AtomicReference<>();
    int rows = delegate.insert(modelName, record, atomicId::set);
    Object id = atomicId.get();
    idConsumer.accept(id);
    return rows;
  }

  @Override
  public <T> T findById(String modelName, Object id, Class<T> resultType, boolean nestedQuery) {
    return delegate.findById(modelName, id, resultType, nestedQuery);
  }

  @Override
  public <T> List<T> find(String modelName, Query query, Class<T> resultType) {
    return delegate.find(modelName, query, resultType);
  }

  @Override
  public <T> List<T> findByNativeQuery(String statement, Map<String, Object> params, Class<T> resultType) {
    return delegate.findByNativeQuery(statement, params, resultType);
  }

  @Override
  public <T> List<T> findByNativeQueryModel(String modelName, Map<String, Object> params, Class<T> resultType) {
    return delegate.findByNativeQueryModel(modelName, params, resultType);
  }

  @Override
  public long count(String modelName, Query query) {
    return delegate.count(modelName, query);
  }

  @Override
  public int updateById(String modelName, Map<String, Object> record, Object id) {
    return delegate.updateById(modelName, record, id);
  }

  @Override
  public int update(String modelName, Map<String, Object> record, String filter) {
    return delegate.update(modelName, record, filter);
  }

  @Override
  public int deleteById(String modelName, Object id) {
    return delegate.deleteById(modelName, id);
  }

  @Override
  public int delete(String modelName, String filter) {
    return delegate.delete(modelName, filter);
  }

  @Override
  public int deleteAll(String modelName) {
    return delegate.deleteAll(modelName);
  }

}
