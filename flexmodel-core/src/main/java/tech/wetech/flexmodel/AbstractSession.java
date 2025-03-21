package tech.wetech.flexmodel;

import tech.wetech.flexmodel.generator.DataOperationsGenerationDecorator;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author cjbi
 */
public abstract class AbstractSession implements Session {

  private final String schemaName;
  private final DataOperations dataOperationsDelegate;
  private final SchemaOperations schemaOperationsDelegate;
  private final SessionFactory factory;

  public AbstractSession(AbstractSessionContext sessionContext, DataOperations dataOperationsDelegate,
                         SchemaOperations schemaOperationsDelegate) {
    this.schemaName = sessionContext.getSchemaName();
    this.dataOperationsDelegate =
      new DataOperationsGenerationDecorator(sessionContext,
        dataOperationsDelegate
      );
    this.schemaOperationsDelegate =
      new SchemaOperationsPersistenceDecorator(sessionContext, schemaOperationsDelegate);
    this.factory = sessionContext.getFactory();
  }

  @Override
  public void dropField(String modelName, String fieldName) {
    schemaOperationsDelegate.dropField(modelName, fieldName);
  }

  @Override
  public void dropIndex(String modelName, String indexName) {
    schemaOperationsDelegate.dropIndex(modelName, indexName);
  }

  @Override
  public List<TypeWrapper> syncModels() {
    return schemaOperationsDelegate.syncModels();
  }

  @Override
  public List<TypeWrapper> syncModels(Set<String> modelNames) {
    return schemaOperationsDelegate.syncModels(modelNames);
  }

  @Override
  public List<TypeWrapper> getAllModels() {
    return schemaOperationsDelegate.getAllModels();
  }

  @Override
  public TypeWrapper getModel(String modelName) {
    return schemaOperationsDelegate.getModel(modelName);
  }

  @Override
  public void dropModel(String modelName) {
    schemaOperationsDelegate.dropModel(modelName);
  }

  @Override
  public Entity createEntity(Entity collection) {
    return schemaOperationsDelegate.createEntity(collection);
  }

  @Override
  public NativeQueryModel createNativeQueryModel(NativeQueryModel model) {
    return schemaOperationsDelegate.createNativeQueryModel(model);
  }

  @Override
  public Enum createEnum(Enum anEnum) {
    return schemaOperationsDelegate.createEnum(anEnum);
  }

  @Override
  public TypedField<?, ?> modifyField(TypedField<?, ?> field) {
    return schemaOperationsDelegate.modifyField(field);
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
  public <T> T findById(String modelName, Object id, Class<T> resultType, boolean nestedQuery) {
    return dataOperationsDelegate.findById(modelName, id, resultType, nestedQuery);
  }

  @Override
  public <T> List<T> find(String modelName, Query query, Class<T> resultType) {
    return dataOperationsDelegate.find(modelName, query, resultType);
  }

  @Override
  public <T> List<T> findByNativeQuery(String statement, Map<String, Object> params, Class<T> resultType) {
    return dataOperationsDelegate.findByNativeQuery(statement, params, resultType);
  }

  @Override
  public <T> List<T> findByNativeQueryModel(String modelName, Map<String, Object> params, Class<T> resultType) {
    return dataOperationsDelegate.findByNativeQueryModel(modelName, params, resultType);
  }

  @Override
  public long count(String modelName, Query query) {
    return dataOperationsDelegate.count(modelName, query);
  }

  @Override
  public SessionFactory getFactory() {
    return factory;
  }

  @Override
  public String getName() {
    return schemaName;
  }

}
