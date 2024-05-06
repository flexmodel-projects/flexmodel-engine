package tech.wetech.flexmodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Multiple data sources test the agent class, and the test results return only the last one
 *
 * @author cjbi
 */
public class MultiDbSessionDelegates implements Session {

  private final Map<String, Session> sessionMap = new HashMap<>();
  private final Logger log = LoggerFactory.getLogger(MultiDbSessionDelegates.class);
  private long timer = 0L;

  public void addDelegate(String key, Session Session) {
    sessionMap.put(key, Session);
  }

  @Override
  public int insert(String modelName, Map<String, Object> record) {
    int lastRows = 0;
    for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
      String key = entry.getKey();
      Session delegate = entry.getValue();
      preExecute(key, delegate);
      lastRows = delegate.insert(modelName, new HashMap<>(record));
      postExecute(key, delegate);
    }
    return lastRows;
  }

  @Override
  public int insert(String modelName, Map<String, Object> record, Consumer<Object> id) {
    int lastRows = 0;
    for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
      String key = entry.getKey();
      Session delegate = entry.getValue();
      preExecute(key, delegate);
      lastRows = delegate.insert(modelName, record, id);
      postExecute(key, delegate);
    }
    return lastRows;
  }

  @Override
  public int insertAll(String modelName, List<Map<String, Object>> records) {
    int lastRows = 0;
    for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
      String key = entry.getKey();
      Session delegate = entry.getValue();
      preExecute(key, delegate);
      lastRows = delegate.insertAll(modelName, records);
      postExecute(key, delegate);
    }
    return lastRows;
  }

  @Override
  public int updateById(String modelName, Map<String, Object> record, Object id) {
    int lastRows = 0;
    for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
      String key = entry.getKey();
      Session delegate = entry.getValue();
      preExecute(key, delegate);
      lastRows = delegate.updateById(modelName, record, id);
      postExecute(key, delegate);
    }
    return lastRows;
  }

  @Override
  public int update(String modelName, Map<String, Object> record, String filter) {
    int lastRows = 0;
    for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
      String key = entry.getKey();
      Session delegate = entry.getValue();
      preExecute(key, delegate);
      lastRows = delegate.update(modelName, record, filter);
      postExecute(key, delegate);
    }
    return lastRows;
  }

  @Override
  public <T> T findById(String modelName, Object id, Class<T> resultType) {
    T lastData = null;
    for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
      String key = entry.getKey();
      Session delegate = entry.getValue();
      preExecute(key, delegate);
      lastData = delegate.findById(modelName, id, resultType);
      postExecute(key, delegate);
    }
    return lastData;
  }

  @Override
  public <T> List<T> find(String modelName, Query query, Class<T> resultType) {
    List<T> lastData = null;
    for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
      String key = entry.getKey();
      Session delegate = entry.getValue();
      preExecute(key, delegate);
      lastData = delegate.find(modelName, query, resultType);
      postExecute(key, delegate);
    }
    return lastData;
  }

  @Override
  public long count(String modelName, Query query) {
    long total = 0L;
    for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
      String key = entry.getKey();
      Session delegate = entry.getValue();
      preExecute(key, delegate);
      total = delegate.count(modelName, query);
      postExecute(key, delegate);
    }
    return total;
  }

  @Override
  public int deleteById(String modelName, Object id) {
    int lastRows = 0;
    for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
      String key = entry.getKey();
      Session delegate = entry.getValue();
      preExecute(key, delegate);
      lastRows = delegate.deleteById(modelName, id);
      postExecute(key, delegate);
    }
    return lastRows;
  }

  @Override
  public int delete(String modelName, String condition) {
    int lastRows = 0;
    for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
      String key = entry.getKey();
      Session delegate = entry.getValue();
      preExecute(key, delegate);
      lastRows = delegate.delete(modelName, condition);
      postExecute(key, delegate);
    }
    return lastRows;
  }

  @Override
  public int deleteAll(String modelName) {
    int lastRows = 0;
    for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
      String key = entry.getKey();
      Session delegate = entry.getValue();
      preExecute(key, delegate);
      lastRows = delegate.deleteAll(modelName);
      postExecute(key, delegate);
    }
    return lastRows;
  }

  @Override
  public List<Model> getAllModels() {
    List<Model> lastModels = new ArrayList<>();
    for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
      String key = entry.getKey();
      Session delegate = entry.getValue();
      preExecute(key, delegate);
      lastModels = delegate.getAllModels();
      postExecute(key, delegate);
    }
    return lastModels;
  }

  @Override
  public Model getModel(String modelName) {
    Model lastModel = null;
    for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
      String key = entry.getKey();
      Session delegate = entry.getValue();
      preExecute(key, delegate);
      lastModel = delegate.getModel(modelName);
      postExecute(key, delegate);
    }
    return lastModel;
  }

  @Override
  public Entity createEntity(String modelName, UnaryOperator<Entity> entityUnaryOperator) {
    Entity lastEntity = null;
    for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
      String key = entry.getKey();
      Session delegate = entry.getValue();
      preExecute(key, delegate);
      lastEntity = delegate.createEntity(modelName, entityUnaryOperator);
      postExecute(key, delegate);
    }
    return lastEntity;
  }

  @Override
  public View createView(String viewName, String viewOn, UnaryOperator<Query> queryUnaryOperator) {
    View lastView = null;
    for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
      String key = entry.getKey();
      Session delegate = entry.getValue();
      preExecute(key, delegate);
      lastView = delegate.createView(viewName, viewOn, queryUnaryOperator);
      postExecute(key, delegate);
    }
    return lastView;
  }

  @Override
  public void dropModel(String modelName) {
    for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
      String key = entry.getKey();
      Session delegate = entry.getValue();
      preExecute(key, delegate);
      delegate.dropModel(modelName);
      postExecute(key, delegate);
    }
  }

  @Override
  public void createField(String modelName, TypedField<?, ?> field) {
    for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
      String key = entry.getKey();
      Session delegate = entry.getValue();
      preExecute(key, delegate);
      delegate.createField(modelName, field);
      postExecute(key, delegate);
    }
  }

  @Override
  public void dropField(String entityName, String fieldName) {
    for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
      String key = entry.getKey();
      Session delegate = entry.getValue();
      preExecute(key, delegate);
      delegate.dropField(entityName, fieldName);
      postExecute(key, delegate);
    }
  }

  @Override
  public void createIndex(Index index) {
    for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
      String key = entry.getKey();
      Session delegate = entry.getValue();
      preExecute(key, delegate);
      delegate.createIndex(index);
      postExecute(key, delegate);
    }
  }

  @Override
  public void dropIndex(String modelName, String indexName) {
    for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
      String key = entry.getKey();
      Session delegate = entry.getValue();
      preExecute(key, delegate);
      delegate.dropIndex(modelName, indexName);
      postExecute(key, delegate);
    }
  }

  @Override
  public void createSequence(String sequenceName, int initialValue, int incrementSize) {
    for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
      String key = entry.getKey();
      Session delegate = entry.getValue();
      preExecute(key, delegate);
      delegate.createSequence(sequenceName, initialValue, incrementSize);
      postExecute(key, delegate);
    }
  }

  @Override
  public void dropSequence(String sequenceName) {
    for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
      String key = entry.getKey();
      Session delegate = entry.getValue();
      preExecute(key, delegate);
      delegate.dropSequence(sequenceName);
      postExecute(key, delegate);
    }
  }

  @Override
  public long getSequenceNextVal(String sequenceName) {
    long lastVal = 0;
    for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
      String key = entry.getKey();
      Session delegate = entry.getValue();
      preExecute(key, delegate);
      lastVal = delegate.getSequenceNextVal(sequenceName);
      postExecute(key, delegate);
    }
    return lastVal;
  }

  private void preExecute(String key, Session Session) {
    log.info("******** Queries Start: {} ********", key);
    timer = System.currentTimeMillis();
  }

  private void postExecute(String key, Session Session) {
    log.info("******** Queries End: {}, Execution Time: {} ms  ********", key, System.currentTimeMillis() - timer);
  }

  @Override
  public void startTransaction() {

  }

  @Override
  public void commit() {

  }

  @Override
  public void rollback() {

  }

  @Override
  public void close() {
    sessionMap.forEach((key, value) -> value.close());
  }
}
