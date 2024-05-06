package tech.wetech.flexmodel.mongodb;

import tech.wetech.flexmodel.AbstractSession;
import tech.wetech.flexmodel.Entity;
import tech.wetech.flexmodel.TypedField;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author cjbi
 */
public class MongoSession extends AbstractSession {

  public MongoSession(MongoContext mongoContext) {
    super(mongoContext.getSchemaName(), mongoContext.getMappedModels(), mongoContext.getTypeHandlerMap(),
      new MongoDataOperations(mongoContext),
      new MongoSchemaOperations(mongoContext));
  }

  @Override
  public void startTransaction() {

  }

  @Override
  public int insert(String modelName, Map<String, Object> record) {
    setId(modelName, record);
    return super.insert(modelName, record);
  }

  private void setId(String modelName, Map<String, Object> record) {
    String sequenceName = modelName + "_seq";
    try {
      createSequence(sequenceName, 1, 1);
    } catch (Exception ignored) {
    }
    long sequenceNextVal = getSequenceNextVal(sequenceName);
    Entity entity = (Entity) getModel(modelName);
    TypedField<?, ?> idField = entity.idField();
    if (idField != null) {
      record.put(idField.name(), sequenceNextVal);
    }
  }

  @Override
  public int insert(String modelName, Map<String, Object> record, Consumer<Object> idConsumer) {
    setId(modelName, record);
    int rows = super.insert(modelName, record, idConsumer);
    Entity entity = (Entity) getModel(modelName);
    TypedField<?, ?> idField = entity.idField();
    idConsumer.accept(record.get(idField.name()));
    return rows;
  }

  @Override
  public int insertAll(String modelName, List<Map<String, Object>> records) {
    for (Map<String, Object> record : records) {
      setId(modelName, record);
    }
    return super.insertAll(modelName, records);
  }

  @Override
  public void dropModel(String modelName) {
    dropSequence(modelName + "_seq");
    super.dropModel(modelName);
  }

  @Override
  public void commit() {

  }

  @Override
  public void rollback() {

  }

  @Override
  public void close() {

  }
}
