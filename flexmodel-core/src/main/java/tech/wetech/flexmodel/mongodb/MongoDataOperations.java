package tech.wetech.flexmodel.mongodb;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import tech.wetech.flexmodel.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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

  public MongoDataOperations(MongoContext mongoContext) {
    this.mongoContext = mongoContext;
    this.schemaName = mongoContext.getSchemaName();
    this.mongoDatabase = mongoContext.getMongoDatabase();
    this.physicalNamingStrategy = mongoContext.getPhysicalNamingStrategy();
    this.mappedModels = mongoContext.getMappedModels();
  }

  @Override
  public int insert(String modelName, Map<String, Object> record) {
    String collectionName = getCollectionName(modelName);

    InsertOneResult result = mongoDatabase.getCollection(collectionName, Map.class).insertOne(record);
    return result.wasAcknowledged() ? 1 : 0;
  }

  private String getCollectionName(String modelName) {
    return physicalNamingStrategy.toPhysicalTableName(modelName);
  }

  @Override
  public int insert(String modelName, Map<String, Object> record, Consumer<Object> idConsumer) {
    String collectionName = getCollectionName(modelName);
    InsertOneResult result = mongoDatabase.getCollection(collectionName, Map.class).insertOne(record);
    return result.wasAcknowledged() ? 1 : 0;
  }

  @Override
  public int insertAll(String modelName, List<Map<String, Object>> records) {
    String collectionName = getCollectionName(modelName);
    InsertManyResult result = mongoDatabase.getCollection(collectionName, Map.class).insertMany(records);
    return result.getInsertedIds().size();
  }

  @Override
  public int updateById(String modelName, Map<String, Object> record, Object id) {
    String collectionName = getCollectionName(modelName);
    Entity entity = mappedModels.getEntity(schemaName, modelName);
    TypedField<?, ?> idField = entity.idField();
    UpdateResult result = mongoDatabase.getCollection(collectionName, Map.class).updateOne(Filters.eq(idField.name(), id), new Document("$set", new Document(record)));
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
    Entity entity = mappedModels.getEntity(schemaName, modelName);
    TypedField<?, ?> idField = entity.idField();
    return (int) mongoDatabase.getCollection(collectionName)
      .deleteMany(Filters.eq(idField.name(), id)).getDeletedCount();
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
  public <T> T findById(String modelName, Object id, Class<T> resultType) {
    String collectionName = getCollectionName(modelName);
    Entity entity = mappedModels.getEntity(schemaName, modelName);
    TypedField<?, ?> idField = entity.idField();

    return mongoDatabase.getCollection(collectionName, resultType)
      .find(Filters.eq(idField.name(), id))
      .first();
  }

  @Override
  public <T> List<T> find(String modelName, Query query, Class<T> resultType) {
    String collectionName = getCollectionName(modelName);
    List<Document> pipeline = MongoHelper.createPipeline(modelName, mongoContext, query);
    return mongoDatabase.getCollection(collectionName, resultType)
      .aggregate(pipeline)
      .into(new ArrayList<>());
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
