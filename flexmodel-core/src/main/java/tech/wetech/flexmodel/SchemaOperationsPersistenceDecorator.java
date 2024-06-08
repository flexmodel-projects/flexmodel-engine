package tech.wetech.flexmodel;

import java.util.List;

/**
 * @author cjbi
 */
class SchemaOperationsPersistenceDecorator implements SchemaOperations {

  private final AbstractSessionContext sessionContext;
  private final SchemaOperations delegate;

  public SchemaOperationsPersistenceDecorator(AbstractSessionContext sessionContext, SchemaOperations delegate) {
    this.sessionContext = sessionContext;
    this.delegate = delegate;
  }

  @Override
  public List<Model> syncModels() {
    return delegate.syncModels();
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
    MappedModels mappedModels = sessionContext.getMappedModels();
    String schemaName = sessionContext.getSchemaName();
    delegate.dropModel(modelName);
    mappedModels.remove(schemaName, modelName);
  }

  @Override
  public Entity createEntity(String modelName, Entity entity) {
    MappedModels mappedModels = sessionContext.getMappedModels();
    String schemaName = sessionContext.getSchemaName();
    mappedModels.persist(schemaName, delegate.createEntity(modelName, entity));
    return entity;
  }

  @Override
  public View createView(String viewName, String viewOn, Query query) {
    MappedModels mappedModels = sessionContext.getMappedModels();
    String schemaName = sessionContext.getSchemaName();
    QueryHelper.validate(schemaName, viewOn, mappedModels, query);
    View view = delegate.createView(viewName, viewOn, query);
    mappedModels.persist(schemaName, view);
    return view;
  }

  @Override
  public TypedField<?, ?> createField(String modelName, TypedField<?, ?> field) {
    MappedModels mappedModels = sessionContext.getMappedModels();
    String schemaName = sessionContext.getSchemaName();
    delegate.createField(modelName, field);
    Entity entity = mappedModels.getEntity(schemaName, field.getModelName());
    entity.addField(field);
    mappedModels.persist(schemaName, entity);
    return field;
  }

  @Override
  public void dropField(String entityName, String fieldName) {
    MappedModels mappedModels = sessionContext.getMappedModels();
    String schemaName = sessionContext.getSchemaName();
    delegate.dropField(entityName, fieldName);
    Entity entity = mappedModels.getEntity(schemaName, entityName);
    entity.removeField(fieldName);
    mappedModels.persist(schemaName, entity);
  }

  @Override
  public Index createIndex(String indexName, Index index) {
    MappedModels mappedModels = sessionContext.getMappedModels();
    String schemaName = sessionContext.getSchemaName();
    delegate.createIndex(indexName, index);
    Entity entity = mappedModels.getEntity(schemaName, index.getModelName());
    entity.addIndex(index);
    mappedModels.persist(schemaName, entity);
    return index;
  }

  @Override
  public void dropIndex(String modelName, String indexName) {
    MappedModels mappedModels = sessionContext.getMappedModels();
    String schemaName = sessionContext.getSchemaName();
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
