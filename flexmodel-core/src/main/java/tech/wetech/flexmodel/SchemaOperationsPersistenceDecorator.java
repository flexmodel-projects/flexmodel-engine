package tech.wetech.flexmodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
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
  public List<TypeWrapper> syncModels() {
    return delegate.syncModels();
  }

  @Override
  public List<TypeWrapper> syncModels(Set<String> modelNames) {
    return delegate.syncModels(modelNames);
  }

  @Override
  public List<TypeWrapper> getAllModels() {
    return delegate.getAllModels();
  }

  @Override
  public TypeWrapper getModel(String modelName) {
    return delegate.getModel(modelName);
  }

  @Override
  public void dropModel(String modelName) {
    inspect(() -> delegate.dropModel(modelName));
    String schemaName = sessionContext.getSchemaName();
    sessionContext.getMappedModels().remove(schemaName, modelName);
  }

  @Override
  public Entity createEntity(Entity collection) {
    inspect(() -> delegate.createEntity(collection));
    String schemaName = sessionContext.getSchemaName();
    sessionContext.getMappedModels().persist(schemaName, collection);
    return collection;
  }

  @Override
  public NativeQueryModel createNativeQueryModel(NativeQueryModel model) {
    String schemaName = sessionContext.getSchemaName();
    MappedModels mappedModels = sessionContext.getMappedModels();
    delegate.createNativeQueryModel(model);
    mappedModels.persist(schemaName, model);
    return model;
  }

  @Override
  public Enum createEnum(Enum anEnum) {
    String schemaName = sessionContext.getSchemaName();
    MappedModels mappedModels = sessionContext.getMappedModels();
    delegate.createEnum(anEnum);
    mappedModels.persist(schemaName, anEnum);
    return anEnum;
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
  public void dropField(String modelName, String fieldName) {
    String schemaName = sessionContext.getSchemaName();
    inspect(() -> delegate.dropField(modelName, fieldName));
    MappedModels mappedModels = sessionContext.getMappedModels();
    Entity entity = (Entity) sessionContext.getModel(modelName);
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
    Entity entity = (Entity) sessionContext.getModel(modelName);
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
      if (!sessionContext.isFailsafe()) {
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
      if (!sessionContext.isFailsafe()) {
        throw e;
      }
      log.warn("Schema error: {}", e.getMessage());
    }
  }

}
