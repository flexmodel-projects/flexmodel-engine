package tech.wetech.flexmodel;

import java.util.List;
import java.util.function.UnaryOperator;

/**
 * @author cjbi
 */
class SchemaOperationsPersistenceDecorator implements SchemaOperations {

  private final String schemaName;
  private final SchemaOperations delegate;
  private final MappedModels mappedModels;

  public SchemaOperationsPersistenceDecorator(String schemaName, MappedModels mappedModels, SchemaOperations delegate) {
    this.schemaName = schemaName;
    this.delegate = delegate;
    this.mappedModels = mappedModels;
  }

  @Override
  public List<Model> getAllModels() {
    return delegate.getAllModels();
  }

  @Override
  public Model getModel(String modelName) {
    return delegate.getModel(modelName);
  }

  @Override
  public void dropModel(String modelName) {
    delegate.dropModel(modelName);
    mappedModels.remove(schemaName, modelName);
  }

  @Override
  public Entity createEntity(String modelName, UnaryOperator<Entity> entityUnaryOperator) {
    Entity entity = delegate.createEntity(modelName, entityUnaryOperator);
    mappedModels.persist(schemaName, entity);
    return entity;
  }

  @Override
  public View createView(String viewName, String viewOn, UnaryOperator<Query> queryUnaryOperator) {
    View view = delegate.createView(viewName, viewOn, queryUnaryOperator);
    mappedModels.persist(schemaName, view);
    return view;
  }

  @Override
  public void createField(String modelName, TypedField<?, ?> field) {
    delegate.createField(modelName, field);
    Entity entity = mappedModels.getEntity(schemaName, field.modelName());
    entity.addField(field);
    mappedModels.persist(schemaName, entity);
  }

  @Override
  public void dropField(String entityName, String fieldName) {
    delegate.dropField(entityName, fieldName);
    Entity entity = mappedModels.getEntity(schemaName, entityName);
    entity.removeField(fieldName);
    mappedModels.persist(schemaName, entity);
  }

  @Override
  public void createIndex(Index index) {
    delegate.createIndex(index);
    Entity entity = mappedModels.getEntity(schemaName, index.getModelName());
    entity.addIndex(index);
    mappedModels.persist(schemaName, entity);
  }

  @Override
  public void dropIndex(String modelName, String indexName) {
    delegate.dropIndex(modelName, indexName);
    Entity entity = mappedModels.getEntity(schemaName, modelName);
    entity.removeIndex(indexName);
    mappedModels.persist(schemaName, entity);
  }

  @Override
  public void createSequence(String sequenceName, int initialValue, int incrementSize) {
    delegate.createSequence(sequenceName, initialValue, incrementSize);
  }

  @Override
  public void dropSequence(String sequenceName) {
    delegate.dropSequence(sequenceName);
  }

  @Override
  public long getSequenceNextVal(String sequenceName) {
    return delegate.getSequenceNextVal(sequenceName);
  }

}
