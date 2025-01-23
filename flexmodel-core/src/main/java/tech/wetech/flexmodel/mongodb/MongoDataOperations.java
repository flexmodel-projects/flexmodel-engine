package tech.wetech.flexmodel.mongodb;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.sql.StringHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static tech.wetech.flexmodel.IDField.GeneratedValue.AUTO_INCREMENT;

/**
 * @author cjbi
 */
public class MongoDataOperations extends BaseMongoStatement implements DataOperations {

  private final String schemaName;
  private final MappedModels mappedModels;
  private final MongoDatabase mongoDatabase;
  private final SchemaOperations schemaOperations;

  public MongoDataOperations(MongoContext mongoContext) {
    super(mongoContext);
    this.schemaName = mongoContext.getSchemaName();
    this.mongoDatabase = mongoContext.getMongoDatabase();
    this.mappedModels = mongoContext.getMappedModels();
    this.schemaOperations = new MongoSchemaOperations(mongoContext);
  }

  private String getCollectionName(String modelName) {
    return modelName;
  }

  @Override
  public int insert(String modelName, Map<String, Object> record, Consumer<Object> idConsumer) {
    Entity entity = (Entity) mappedModels.getModel(schemaName, modelName);
    IDField idField = entity.findIdField().orElseThrow();
    if (!record.containsKey(idField.getName()) && idField.getGeneratedValue() == AUTO_INCREMENT) {
      setId(modelName, record);
      idConsumer.accept(record.get(idField.getName()));
    }
    String collectionName = getCollectionName(modelName);
    InsertOneResult result = mongoDatabase.getCollection(collectionName, Map.class).insertOne(record);
    idConsumer.accept(record.get(idField.getName()));
    return result.wasAcknowledged() ? 1 : 0;
  }

  private void setId(String modelName, Map<String, Object> record) {
    String sequenceName = modelName + "_seq";
    try {
      schemaOperations.createSequence(sequenceName, 1, 1);
    } catch (Exception ignored) {
    }
    long sequenceNextVal = schemaOperations.getSequenceNextVal(sequenceName);
    Entity entity = (Entity) schemaOperations.getModel(modelName);
    TypedField<?, ?> idField = entity.findIdField().orElseThrow();
    record.put(idField.getName(), sequenceNextVal);
  }

  @Override
  public int updateById(String modelName, Map<String, Object> record, Object id) {
    String collectionName = getCollectionName(modelName);
    Entity entity = (Entity) mappedModels.getModel(schemaName, modelName);
    TypedField<?, ?> idField = entity.findIdField().orElseThrow();
    UpdateResult result = mongoDatabase.getCollection(collectionName, Map.class).updateOne(Filters.eq(idField.getName(), id), new Document("$set", new Document(record)));
    return (int) result.getModifiedCount();
  }

  @Override
  public int update(String modelName, Map<String, Object> record, String filter) {
    String collectionName = getCollectionName(modelName);
    String mongoCondition = getMongoCondition(filter);
    Document mongoFilter = Document.parse(mongoCondition);
    UpdateResult result = mongoDatabase.getCollection(collectionName).updateMany(mongoFilter, new Document("$set", new Document(record)));
    return (int) result.getModifiedCount();
  }

  @Override
  public int deleteById(String modelName, Object id) {
    String collectionName = getCollectionName(modelName);
    Entity entity = (Entity) mappedModels.getModel(schemaName, modelName);
    TypedField<?, ?> idField = entity.findIdField().orElseThrow();
    return (int) mongoDatabase.getCollection(collectionName)
      .deleteMany(Filters.eq(idField.getName(), id)).getDeletedCount();
  }

  @Override
  public int delete(String modelName, String filter) {
    String collectionName = getCollectionName(modelName);
    String mongoCondition = getMongoCondition(filter);
    return (int) mongoDatabase.getCollection(collectionName)
      .deleteMany(Document.parse(mongoCondition)).getDeletedCount();
  }

  @Override
  public int deleteAll(String modelName) {
    String collectionName = getCollectionName(modelName);
    return (int) mongoDatabase.getCollection(collectionName)
      .deleteMany(Filters.empty()).getDeletedCount();

  }

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public <T> T findById(String modelName, Object id, Class<T> resultType, boolean nestedQuery) {
    String collectionName = getCollectionName(modelName);
    Entity entity = (Entity) mappedModels.getModel(schemaName, modelName);
    TypedField<?, ?> idField = entity.findIdField().orElseThrow();

    Map dataMap = mongoDatabase.getCollection(collectionName, Map.class)
      .find(Filters.eq(idField.getName(), id))
      .first();
    if (nestedQuery && dataMap != null) {
      QueryHelper.nestedQuery(List.of(dataMap), this::findMapList, mongoContext.getModel(modelName),
        null, mongoContext, mongoContext.getNestedQueryMaxDepth());
    }
    return mongoContext.getJsonObjectConverter().convertValue(dataMap, resultType);
  }

  @Override
  public <T> List<T> find(String modelName, Query query, Class<T> resultType) {
    List<Map<String, Object>> mapList = findMapList(modelName, query);
    QueryHelper.nestedQuery(mapList, this::findMapList, mongoContext.getModel(modelName), query, mongoContext, mongoContext.getNestedQueryMaxDepth());
    return mongoContext.getJsonObjectConverter().convertValueList(mapList, resultType);
  }

  @Override
  public <T> List<T> findByNativeQuery(String statement, Map<String, Object> params, Class<T> resultType) {
    String json = StringHelper.simpleRenderTemplate(statement, params);
    Document result = mongoDatabase.runCommand(Document.parse(json));
    List<?> list = (List<?>) ((Map<?, ?>) result.get("cursor")).get("firstBatch");
    return mongoContext.getJsonObjectConverter().convertValueList(list, resultType);
  }

  @Override
  public <T> List<T> findByNativeQueryModel(String modelName, Map<String, Object> params, Class<T> resultType) {
    NativeQueryModel model = (NativeQueryModel) mongoContext.getModel(modelName);
    return findByNativeQuery(model.getStatement(), params, resultType);
  }

  @SuppressWarnings({"rawtypes"})
  private List<Map<String, Object>> findMapList(String modelName, Query query) {
    List<Document> pipeline = createPipeline(modelName, query);

    String collectionName = getCollectionName(modelName);
    List list = mongoDatabase.getCollection(collectionName, Map.class)
      .aggregate(pipeline)
      .into(new ArrayList<>());
    return list;
  }

  @Override
  public long count(String modelName, Query query) {
    String collectionName = getCollectionName(modelName);
    List<Document> pipeline = createPipeline(modelName, query);
    ArrayList<Document> count = new ArrayList<>(pipeline);
    count.add(new Document("$count", "total"));
    Number total = mongoDatabase.getCollection(collectionName)
      .aggregate(count).map(map -> map.get("total", Number.class))
      .first();
    return total != null ? total.longValue() : 0;
  }

}
