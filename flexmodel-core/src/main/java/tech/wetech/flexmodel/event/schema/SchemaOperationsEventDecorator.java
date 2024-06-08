package tech.wetech.flexmodel.event.schema;

import tech.wetech.flexmodel.*;

import java.util.List;

/**
 * @author cjbi
 */
public class SchemaOperationsEventDecorator implements SchemaOperations {

  private final AbstractSessionContext sessionContext;

  private final SchemaOperations delegate;

  public SchemaOperationsEventDecorator(AbstractSessionContext sessionContext, SchemaOperations delegate) {
    this.sessionContext = sessionContext;
    this.delegate = delegate;
  }

  @Override
  public List<Model> syncModels() {
    sessionContext.publishEvent(new SyncModelEvent(sessionContext.getSchemaName()));
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
    String schemaName = sessionContext.getSchemaName();
    Model model = sessionContext.getModel(modelName);
    sessionContext.publishEvent(new PreDropModelEvent(schemaName, model));
    delegate.dropModel(modelName);
    sessionContext.publishEvent(new PostDropModelEvent(schemaName, model));
  }

  @Override
  public Entity createEntity(Entity entity) {
    sessionContext.publishEvent(new PreCreateEntityEvent(sessionContext.getSchemaName(), entity));
    Entity rE = delegate.createEntity(entity);
    sessionContext.publishEvent(new PostCreateEntityEvent(sessionContext.getSchemaName(), rE));
    return rE;
  }

  @Override
  public View createView(String viewName, String viewOn, Query query) {
    sessionContext.publishEvent(new PreCreateViewEvent(sessionContext.getSchemaName(), viewName, viewOn, query));
    View view = delegate.createView(viewName, viewOn, query);
    sessionContext.publishEvent(new PostCreateViewEvent(sessionContext.getSchemaName(), view));
    return view;
  }

  @Override
  public TypedField<?, ?> createField(TypedField<?, ?> field) {
    Entity entity = (Entity) sessionContext.getModel(field.getModelName());
    sessionContext.publishEvent(new PreCreateFieldEvent(sessionContext.getSchemaName(), entity, field));
    TypedField<?, ?> result = delegate.createField(field);
    sessionContext.publishEvent(new PostCreateFieldEvent(sessionContext.getSchemaName(), entity, field));
    return result;
  }

  @Override
  public void dropField(String entityName, String fieldName) {
    Entity entity = (Entity) sessionContext.getModel(entityName);
    TypedField<?, ?> field = (TypedField<?, ?>) entity.getField(fieldName);
    sessionContext.publishEvent(new PreCreateFieldEvent(sessionContext.getSchemaName(), entity, field));
    delegate.dropField(entityName, fieldName);
    sessionContext.publishEvent(new PostCreateFieldEvent(sessionContext.getSchemaName(), entity, field));
  }

  @Override
  public Index createIndex(Index index) {
    Entity entity = (Entity) sessionContext.getModel(index.getModelName());
    sessionContext.publishEvent(new PreCreateIndexEvent(sessionContext.getSchemaName(), entity, index));
    Index result = delegate.createIndex(index);
    sessionContext.publishEvent(new PostCreateIndexEvent(sessionContext.getSchemaName(), entity, index));
    return result;
  }

  @Override
  public void dropIndex(String modelName, String indexName) {
    Entity entity = (Entity) sessionContext.getModel(modelName);
    Index index = entity.getIndexes().stream()
      .filter(i -> i.getName().equals(indexName)).findFirst()
      .orElse(null);
    sessionContext.publishEvent(new PreCreateIndexEvent(sessionContext.getSchemaName(), entity, index));
    delegate.dropIndex(modelName, indexName);
    sessionContext.publishEvent(new PostCreateIndexEvent(sessionContext.getSchemaName(), entity, index));
  }

  @Override
  public void createSequence(String sequenceName, int initialValue, int incrementSize) {
    sessionContext.publishEvent(new PreCreateSequenceEvent(sessionContext.getSchemaName(), sequenceName, initialValue, incrementSize));
    delegate.createSequence(sequenceName, initialValue, incrementSize);
    sessionContext.publishEvent(new PostCreateSequenceEvent(sessionContext.getSchemaName(), sequenceName, initialValue, incrementSize));
  }

  @Override
  public void dropSequence(String sequenceName) {
    sessionContext.publishEvent(new PreDropSequenceEvent(sessionContext.getSchemaName(), sequenceName));
    delegate.dropSequence(sequenceName);
    sessionContext.publishEvent(new PostDropSequenceEvent(sessionContext.getSchemaName(), sequenceName));
  }

  @Override
  public long getSequenceNextVal(String sequenceName) {
    return delegate.getSequenceNextVal(sequenceName);
  }

}
