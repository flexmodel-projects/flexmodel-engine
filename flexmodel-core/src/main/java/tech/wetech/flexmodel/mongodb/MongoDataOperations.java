package tech.wetech.flexmodel.mongodb;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import tech.wetech.flexmodel.model.EntityDefinition;
import tech.wetech.flexmodel.model.ModelDefinition;
import tech.wetech.flexmodel.model.NativeQueryDefinition;
import tech.wetech.flexmodel.model.field.GeneratedValue;
import tech.wetech.flexmodel.model.field.TypedField;
import tech.wetech.flexmodel.operation.DataOperations;
import tech.wetech.flexmodel.operation.SchemaOperations;
import tech.wetech.flexmodel.query.Query;
import tech.wetech.flexmodel.query.QueryHelper;
import tech.wetech.flexmodel.reflect.ReflectionUtils;
import tech.wetech.flexmodel.sql.StringHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
public class MongoDataOperations extends BaseMongoStatement implements DataOperations {

  private final MongoDatabase mongoDatabase;
  private final SchemaOperations schemaOperations;

  public MongoDataOperations(MongoContext mongoContext) {
    super(mongoContext);
    this.mongoDatabase = mongoContext.getMongoDatabase();
    this.schemaOperations = new MongoSchemaOperations(mongoContext);
  }

  private String getCollectionName(String modelName) {
    return modelName;
  }

  @Override
  public int insert(String modelName, Object objR) {
    Map<String, Object> record = ReflectionUtils.toClassBean(mongoContext.getJsonObjectConverter(), objR, Map.class);
    EntityDefinition entity = (EntityDefinition) mongoContext.getModel(modelName);
    TypedField<?, ?> idField = entity.findIdField().orElseThrow();
    if (!record.containsKey(idField.getName()) && idField.getDefaultValue().equals(GeneratedValue.AUTO_INCREMENT)) {
      setId(modelName, record);
    }
    String collectionName = getCollectionName(modelName);
    InsertOneResult result = mongoDatabase.getCollection(collectionName, Map.class).insertOne(record);
    return result.wasAcknowledged() ? 1 : 0;
  }

  private void setId(String modelName, Map<String, Object> record) {
    String sequenceName = modelName + "_seq";
    try {
      schemaOperations.createSequence(sequenceName, 1, 1);
    } catch (Exception ignored) {
    }
    long sequenceNextVal = schemaOperations.getSequenceNextVal(sequenceName);
    EntityDefinition entity = (EntityDefinition) schemaOperations.getModel(modelName);
    TypedField<?, ?> idField = entity.findIdField().orElseThrow();
    record.put(idField.getName(), sequenceNextVal);
  }

  @Override
  public int updateById(String modelName, Object objR, Object id) {
    Map<String, Object> record = ReflectionUtils.toClassBean(mongoContext.getJsonObjectConverter(), objR, Map.class);
    String collectionName = getCollectionName(modelName);
    EntityDefinition entity = (EntityDefinition) mongoContext.getModel(modelName);
    TypedField<?, ?> idField = entity.findIdField().orElseThrow();
    UpdateResult result = mongoDatabase.getCollection(collectionName, Map.class).updateOne(Filters.eq(idField.getName(), id), new Document("$set", new Document(record)));
    return (int) result.getModifiedCount();
  }

  @Override
  public int update(String modelName, Object objR, String filter) {
    Map<String, Object> record = ReflectionUtils.toClassBean(mongoContext.getJsonObjectConverter(), objR, Map.class);
    String collectionName = getCollectionName(modelName);
    String mongoCondition = getMongoCondition(filter);
    Document mongoFilter = Document.parse(mongoCondition);
    UpdateResult result = mongoDatabase.getCollection(collectionName).updateMany(mongoFilter, new Document("$set", new Document(record)));
    return (int) result.getModifiedCount();
  }

  @Override
  public int deleteById(String modelName, Object id) {
    String collectionName = getCollectionName(modelName);
    EntityDefinition entity = (EntityDefinition) mongoContext.getModel(modelName);
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
    EntityDefinition entity = (EntityDefinition) mongoContext.getModel(modelName);
    TypedField<?, ?> idField = entity.findIdField().orElseThrow();

    Map dataMap = mongoDatabase.getCollection(collectionName, Map.class)
      .find(Filters.eq(idField.getName(), id))
      .first();
    if (nestedQuery && dataMap != null) {
      QueryHelper.nestedQuery(List.of(dataMap), this::findMapList, (ModelDefinition) mongoContext.getModel(modelName),
        null, mongoContext, mongoContext.getNestedQueryMaxDepth());
    }
    return mongoContext.getJsonObjectConverter().convertValue(dataMap, resultType);
  }

  @Override
  public <T> List<T> find(String modelName, Query query, Class<T> resultType) {
    List<Map<String, Object>> mapList = findMapList(modelName, query);
    if (query.isNestedQueryEnabled()) {
      QueryHelper.nestedQuery(mapList, this::findMapList, (ModelDefinition) mongoContext.getModel(modelName), query, mongoContext, mongoContext.getNestedQueryMaxDepth());
    }
    return mongoContext.getJsonObjectConverter().convertValueList(mapList, resultType);
  }

  @Override
  public <T> List<T> findByNativeQueryStatement(String statement, Object obj, Class<T> resultType) {
    Map<String, Object> params = ReflectionUtils.toClassBean(mongoContext.getJsonObjectConverter(), obj, Map.class);
    String json = StringHelper.simpleRenderTemplate(statement, params);
    Document result = mongoDatabase.runCommand(Document.parse(json));
    List<?> list = (List<?>) ((Map<?, ?>) result.get("cursor")).get("firstBatch");
    return mongoContext.getJsonObjectConverter().convertValueList(list, resultType);
  }

  @Override
  public <T> List<T> findByNativeQueryModel(String modelName, Object obj, Class<T> resultType) {
    Map<String, Object> params = ReflectionUtils.toClassBean(mongoContext.getJsonObjectConverter(), obj, Map.class);
    NativeQueryDefinition model = (NativeQueryDefinition) mongoContext.getModel(modelName);
    return findByNativeQueryStatement(model.getStatement(), params, resultType);
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
