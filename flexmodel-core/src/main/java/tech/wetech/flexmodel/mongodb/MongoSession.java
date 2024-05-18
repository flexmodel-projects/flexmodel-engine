package tech.wetech.flexmodel.mongodb;

import tech.wetech.flexmodel.AbstractSession;

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
  public int insert(String modelName, Map<String, Object> record, Consumer<Object> idConsumer) {
    return super.insert(modelName, record, idConsumer);
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
