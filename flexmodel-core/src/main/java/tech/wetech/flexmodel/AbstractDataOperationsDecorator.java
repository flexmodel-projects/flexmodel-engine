package tech.wetech.flexmodel;

import tech.wetech.flexmodel.event.record.*;
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
    sessionContext.publishEvent(new PreInsertRecordEvent(sessionContext.getSchemaName(), modelName, record));
    AtomicReference<Object> atomicId = new AtomicReference<>();
    int rows = delegate.insert(modelName, record, atomicId::set);
    Object id = atomicId.get();
    idConsumer.accept(id);
    sessionContext.publishEvent(new PostInsertRecordEvent(sessionContext.getSchemaName(), modelName, record, id, rows));
    return rows;
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
    sessionContext.publishEvent(new PreUpdateRecordEvent(sessionContext.getSchemaName(), modelName, record, id, null));
    int rows = delegate.updateById(modelName, record, id);
    sessionContext.publishEvent(new PostUpdateRecordEvent(sessionContext.getSchemaName(), modelName, record, id, null, rows));
    return rows;
  }

  @Override
  public int update(String modelName, Map<String, Object> record, String filter) {
    sessionContext.publishEvent(new PreUpdateRecordEvent(sessionContext.getSchemaName(), modelName, record, null, filter));
    int rows = delegate.update(modelName, record, filter);
    sessionContext.publishEvent(new PostUpdateRecordEvent(sessionContext.getSchemaName(), modelName, record, null, filter, rows));
    return rows;
  }

  @Override
  public int deleteById(String modelName, Object id) {
    sessionContext.publishEvent(new PreDeleteRecordEvent(sessionContext.getSchemaName(), modelName, id, null));
    int rows = delegate.deleteById(modelName, id);
    sessionContext.publishEvent(new PostDeleteRecordEvent(sessionContext.getSchemaName(), modelName, id, null, rows));
    return rows;
  }

  @Override
  public int delete(String modelName, String filter) {
    sessionContext.publishEvent(new PreDeleteRecordEvent(sessionContext.getSchemaName(), modelName, null, filter));
    int rows = delegate.delete(modelName, filter);
    sessionContext.publishEvent(new PostDeleteRecordEvent(sessionContext.getSchemaName(), modelName, null, filter, rows));
    return rows;
  }

  @Override
  public int deleteAll(String modelName) {
    sessionContext.publishEvent(new PreDeleteRecordEvent(sessionContext.getSchemaName(), modelName, null, null));
    int rows = delegate.deleteAll(modelName);
    sessionContext.publishEvent(new PostDeleteRecordEvent(sessionContext.getSchemaName(), modelName, null, null, rows));
    return rows;
  }

}
