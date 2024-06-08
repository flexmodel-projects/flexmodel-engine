package tech.wetech.flexmodel;

import tech.wetech.flexmodel.event.record.DataOperationsEventDecorator;
import tech.wetech.flexmodel.event.schema.SchemaOperationsEventDecorator;
import tech.wetech.flexmodel.generations.DataOperationsGenerationDecorator;
import tech.wetech.flexmodel.graph.JoinGraphNode;
import tech.wetech.flexmodel.validations.DataOperationsValidationDecorator;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author cjbi
 */
public abstract class AbstractSession implements Session {

  private final DataOperations dataOperationsDelegate;
  private final SchemaOperations schemaOperationsDelegate;

  public AbstractSession(AbstractSessionContext sessionContext, DataOperations dataOperationsDelegate,
                         SchemaOperations schemaOperationsDelegate) {
    this.dataOperationsDelegate =
      new DataOperationsEventDecorator(
        sessionContext,
        new DataOperationsGenerationDecorator(sessionContext,
          new DataOperationsValidationDecorator(sessionContext,
            dataOperationsDelegate
          )
        )
      );
    this.schemaOperationsDelegate =
      new SchemaOperationsEventDecorator(
        sessionContext,
        new SchemaOperationsPersistenceDecorator(sessionContext, schemaOperationsDelegate));
  }

  @Override
  public void dropField(String entityName, String fieldName) {
    schemaOperationsDelegate.dropField(entityName, fieldName);
  }

  @Override
  public void dropIndex(String modelName, String indexName) {
    schemaOperationsDelegate.dropIndex(modelName, indexName);
  }

  @Override
  public List<Model> syncModels() {
    return schemaOperationsDelegate.syncModels();
  }

  @Override
  public List<Model> getAllModels() {
    return schemaOperationsDelegate.getAllModels();
  }

  @Override
  public Model getModel(String modelName) {
    return schemaOperationsDelegate.getModel(modelName);
  }

  @Override
  public void dropModel(String modelName) {
    schemaOperationsDelegate.dropModel(modelName);
  }

  @Override
  public Entity createEntity(Entity entity) {
    return schemaOperationsDelegate.createEntity(entity);
  }

  @Override
  public TypedField<?, ?> createField(TypedField<?, ?> field) {
    return schemaOperationsDelegate.createField(field);
  }

  @Override
  public Index createIndex(Index index) {
    return schemaOperationsDelegate.createIndex(index);
  }

  @Override
  public void createSequence(String sequenceName, int initialValue, int incrementSize) {
    schemaOperationsDelegate.createSequence(sequenceName, initialValue, incrementSize);
  }

  @Override
  public void dropSequence(String sequenceName) {
    schemaOperationsDelegate.dropSequence(sequenceName);
  }

  @Override
  public long getSequenceNextVal(String sequenceName) {
    return schemaOperationsDelegate.getSequenceNextVal(sequenceName);
  }

  @Override
  public int insert(String modelName, Map<String, Object> record, Consumer<Object> id) {
    return dataOperationsDelegate.insert(modelName, record, id);
  }

  @Override
  public int updateById(String modelName, Map<String, Object> record, Object id) {
    return dataOperationsDelegate.updateById(modelName, record, id);
  }

  @Override
  public int update(String modelName, Map<String, Object> record, String filter) {
    return dataOperationsDelegate.update(modelName, record, filter);
  }

  @Override
  public int deleteById(String modelName, Object id) {
    return dataOperationsDelegate.deleteById(modelName, id);
  }

  @Override
  public int delete(String modelName, String condition) {
    return dataOperationsDelegate.delete(modelName, condition);
  }

  @Override
  public int deleteAll(String modelName) {
    return dataOperationsDelegate.deleteAll(modelName);
  }

  @Override
  public <T> T findById(String modelName, Object id, Class<T> resultType) {
    return dataOperationsDelegate.findById(modelName, id, resultType);
  }

  @Override
  public <T> List<T> find(String modelName, Query query, Class<T> resultType) {
    return dataOperationsDelegate.find(modelName, query, resultType);
  }

  @Override
  public long count(String modelName, Query query) {
    return dataOperationsDelegate.count(modelName, query);
  }

  @Override
  public View createView(String viewName, String viewOn, Query query) {
    return schemaOperationsDelegate.createView(viewName, viewOn, query);
  }

  @Override
  public void associate(JoinGraphNode joinGraphNode, Map<String, Object> data) {
    dataOperationsDelegate.associate(joinGraphNode, data);
  }
}
