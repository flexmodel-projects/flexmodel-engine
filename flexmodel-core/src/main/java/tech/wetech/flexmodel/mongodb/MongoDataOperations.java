package tech.wetech.flexmodel.mongodb;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.graph.JoinGraphNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static tech.wetech.flexmodel.IDField.GeneratedValue.AUTO_INCREMENT;
import static tech.wetech.flexmodel.mongodb.MongoHelper.getMongoCondition;

/**
 * @author cjbi
 */
public class MongoDataOperations implements DataOperations {

  private final MongoContext mongoContext;
  private final String schemaName;
  private final MappedModels mappedModels;
  private final MongoDatabase mongoDatabase;
  private final PhysicalNamingStrategy physicalNamingStrategy;
  private final SchemaOperations schemaOperations;

  public MongoDataOperations(MongoContext mongoContext) {
    this.mongoContext = mongoContext;
    this.schemaName = mongoContext.getSchemaName();
    this.mongoDatabase = mongoContext.getMongoDatabase();
    this.physicalNamingStrategy = mongoContext.getPhysicalNamingStrategy();
    this.mappedModels = mongoContext.getMappedModels();
    this.schemaOperations = new MongoSchemaOperations(mongoContext);
  }

  private String getCollectionName(String modelName) {
    return physicalNamingStrategy.toPhysicalTableName(modelName);
  }

  @Override
  public void associate(JoinGraphNode joinGraphNode, Map<String, Object> data) {
    String collectionName = getCollectionName(joinGraphNode.getJoinName());
    mongoDatabase.getCollection(collectionName, Map.class).insertOne(data);
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
    String mongoCondition = MongoHelper.getMongoCondition(mongoContext, filter);
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
    String mongoCondition = getMongoCondition(mongoContext, filter);
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
  public <T> T findById(String modelName, Object id, Class<T> resultType, boolean deep) {
    String collectionName = getCollectionName(modelName);
    Entity entity = (Entity) mappedModels.getModel(schemaName, modelName);
    TypedField<?, ?> idField = entity.findIdField().orElseThrow();

    Map dataMap = mongoDatabase.getCollection(collectionName, Map.class)
      .find(Filters.eq(idField.getName(), id))
      .first();
    if (deep && dataMap != null) {
      QueryHelper.deepQuery(List.of(dataMap), this::findMapList, mongoContext.getModel(modelName),
        null, mongoContext, mongoContext.getDeepQueryMaxDepth());
    }
    return mongoContext.getJsonObjectConverter().convertValue(dataMap, resultType);
  }

  @Override
  public <T> List<T> find(String modelName, Query query, Class<T> resultType) {
    List<Map<String, Object>> mapList = findMapList(modelName, query);
    QueryHelper.deepQuery(mapList, this::findMapList, mongoContext.getModel(modelName), query, mongoContext, mongoContext.getDeepQueryMaxDepth());
    return mongoContext.getJsonObjectConverter().convertValueList(mapList, resultType);
  }

  @SuppressWarnings({"rawtypes"})
  private List<Map<String, Object>> findMapList(String modelName, Query query) {
    List<Document> pipeline = MongoHelper.createPipeline(modelName, mongoContext, query);

    String collectionName = getCollectionName(modelName);
    List list = mongoDatabase.getCollection(collectionName, Map.class)
      .aggregate(pipeline)
      .into(new ArrayList<>());
    return list;
  }

  @Override
  public long count(String modelName, Query query) {
    String collectionName = getCollectionName(modelName);
    List<Document> pipeline = MongoHelper.createPipeline(modelName, mongoContext, query);
    ArrayList<Document> count = new ArrayList<>(pipeline);
    count.add(new Document("$count", "total"));
    Number total = mongoDatabase.getCollection(collectionName)
      .aggregate(count).map(map -> map.get("total", Number.class))
      .first();
    return total != null ? total.longValue() : 0;
  }

}
