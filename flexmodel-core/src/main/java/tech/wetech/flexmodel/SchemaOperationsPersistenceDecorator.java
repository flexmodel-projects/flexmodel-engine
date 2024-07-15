package tech.wetech.flexmodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Supplier;

/**
 * @author cjbi
 */
class SchemaOperationsPersistenceDecorator implements SchemaOperations {

  private final AbstractSessionContext sessionContext;
  private final SchemaOperations delegate;
  private final Logger log = LoggerFactory.getLogger(SchemaOperationsPersistenceDecorator.class);

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
    inspect(() -> delegate.dropModel(modelName));
    String schemaName = sessionContext.getSchemaName();
    sessionContext.getMappedModels().remove(schemaName, modelName);
  }

  @Override
  public Entity createEntity(Entity entity) {
    inspect(() -> delegate.createEntity(entity));
    String schemaName = sessionContext.getSchemaName();
    sessionContext.getMappedModels().persist(schemaName, entity);
    return entity;
  }

  @Override
  public View createView(String viewName, String viewOn, Query query) {
    String schemaName = sessionContext.getSchemaName();
    MappedModels mappedModels = sessionContext.getMappedModels();
    View view = inspect(() -> delegate.createView(viewName, viewOn, query), null);
    mappedModels.persist(schemaName, view);
    return view;
  }

  @Override
  public TypedField<?, ?> modifyField(TypedField<?, ?> field) {
    inspect(() -> delegate.modifyField(field));
    MappedModels mappedModels = sessionContext.getMappedModels();
    String schemaName = sessionContext.getSchemaName();
    Entity entity = (Entity) mappedModels.getModel(schemaName, field.getModelName());
    entity.removeField(field.getName());
    entity.addField(field);
    mappedModels.persist(schemaName, entity);
    return field;
  }

  @Override
  public TypedField<?, ?> createField(TypedField<?, ?> field) {
    inspect(() -> delegate.createField(field));
    String schemaName = sessionContext.getSchemaName();
    MappedModels mappedModels = sessionContext.getMappedModels();
    Entity entity = (Entity) mappedModels.getModel(schemaName, field.getModelName());
    entity.addField(field);
    mappedModels.persist(schemaName, entity);
    return field;
  }

  @Override
  public void dropField(String entityName, String fieldName) {
    String schemaName = sessionContext.getSchemaName();
    inspect(() -> delegate.dropField(entityName, fieldName));
    MappedModels mappedModels = sessionContext.getMappedModels();
    Entity entity = (Entity) mappedModels.getModel(schemaName, entityName);
    entity.removeField(fieldName);
    for (Index index : entity.getIndexes()) {
      index.containsField(fieldName);
      entity.removeIndex(index.getName());
    }
    mappedModels.persist(schemaName, entity);
  }

  @Override
  public Index createIndex(Index index) {
    inspect(() -> delegate.createIndex(index));
    String schemaName = sessionContext.getSchemaName();
    MappedModels mappedModels = sessionContext.getMappedModels();
    Entity entity = (Entity) mappedModels.getModel(schemaName, index.getModelName());
    entity.addIndex(index);
    mappedModels.persist(schemaName, entity);
    return index;
  }

  @Override
  public void dropIndex(String modelName, String indexName) {
    inspect(() -> delegate.dropIndex(modelName, indexName));
    String schemaName = sessionContext.getSchemaName();
    MappedModels mappedModels = sessionContext.getMappedModels();
    Entity entity = (Entity) mappedModels.getModel(schemaName, modelName);
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

  private <T> T inspect(Supplier<T> supplier, T orElse) {
    try {
      return supplier.get();
    } catch (Exception e) {
      if (sessionContext.isFailFast()) {
        throw e;
      }
      log.warn("Schema error: {}", e.getMessage());
      return orElse;
    }
  }

  @FunctionalInterface
  interface NoArg {
    void exec();
  }

  private void inspect(NoArg noArg) {
    try {
      noArg.exec();
    } catch (Exception e) {
      if (sessionContext.isFailFast()) {
        throw e;
      }
      log.warn("Schema error: {}", e.getMessage());
    }
  }

}
