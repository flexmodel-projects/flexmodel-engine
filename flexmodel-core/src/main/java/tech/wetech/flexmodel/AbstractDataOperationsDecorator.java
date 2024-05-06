package tech.wetech.flexmodel;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * @author cjbi
 */
public class AbstractDataOperationsDecorator implements DataOperations {


  protected final DataOperations delegate;

  public AbstractDataOperationsDecorator(DataOperations delegate) {
    this.delegate = delegate;
  }

  @Override
  public int insert(String modelName, Map<String, Object> record) {
    return delegate.insert(modelName, record);
  }

  @Override
  public int insert(String modelName, Map<String, Object> record, Consumer<Object> id) {
    return delegate.insert(modelName, record, id);
  }

  @Override
  public int insertAll(String modelName, List<Map<String, Object>> records) {
    return delegate.insertAll(modelName, records);
  }

  @Override
  public <T> T findById(String modelName, Object id, Class<T> resultType) {
    return delegate.findById(modelName, id, resultType);
  }

  @Override
  public <T> List<T> find(String modelName, Query query, Class<T> resultType) {
    return delegate.find(modelName, query, resultType);
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

  @Override
  public <T> List<T> find(String modelName, UnaryOperator<Query> queryUnaryOperator, Class<T> resultType) {
    return delegate.find(modelName, queryUnaryOperator, resultType);
  }

  @Override
  public Map<String, Object> findById(String modelName, Object id) {
    return delegate.findById(modelName, id);
  }

  @Override
  public List<Map<String, Object>> find(String modelName, UnaryOperator<Query> queryUnaryOperator) {
    return delegate.find(modelName, queryUnaryOperator);
  }

  @Override
  public boolean existsById(String modelName, Object id) {
    return delegate.existsById(modelName, id);
  }

}
