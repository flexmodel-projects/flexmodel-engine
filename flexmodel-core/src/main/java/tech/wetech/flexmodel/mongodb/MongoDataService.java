package tech.wetech.flexmodel.mongodb;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import tech.wetech.flexmodel.model.EntityDefinition;
import tech.wetech.flexmodel.model.ModelDefinition;
import tech.wetech.flexmodel.model.NativeQueryDefinition;
import tech.wetech.flexmodel.model.field.DefaultValue;
import tech.wetech.flexmodel.model.field.TypedField;
import tech.wetech.flexmodel.query.Query;
import tech.wetech.flexmodel.service.BaseService;
import tech.wetech.flexmodel.service.DataService;
import tech.wetech.flexmodel.service.SchemaService;
import tech.wetech.flexmodel.sql.StringHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author cjbi
 */
public class MongoDataService extends BaseService implements DataService {

  private final MongoDatabase mongoDatabase;
  private final SchemaService schemaService;
  private final MongoStatementBuilder builder;
  private final MongoContext sessionContext;

  public MongoDataService(MongoContext sessionContext) {
    super(sessionContext);
    this.mongoDatabase = sessionContext.getMongoDatabase();
    this.schemaService = new MongoSchemaService(sessionContext);
    this.sessionContext = sessionContext;
    this.builder = new MongoStatementBuilder(sessionContext);
  }

  private String getCollectionName(String modelName) {
    return modelName;
  }

  @Override
  public int insert(String modelName, Map<String, Object> data) {

    Map<String, Object> processedData = generateFieldValues(modelName, data, false);

    try {
      EntityDefinition entity = (EntityDefinition) sessionContext.getModelDefinition(modelName);
      TypedField<?, ?> idField = entity.findIdField().orElseThrow();
      DefaultValue defaultValue = idField.getDefaultValue();
      if (defaultValue != null && defaultValue.isGenerated() && "autoIncrement".equals(defaultValue.getName())) {
        assignAutoIncrementId(modelName, processedData);
      }
      String collectionName = getCollectionName(modelName);
      InsertOneResult result = mongoDatabase.getCollection(collectionName, Map.class).insertOne(processedData);
      return result.wasAcknowledged() ? 1 : 0;
    } finally {
      // 获取生成的ID（如果有的话）
      EntityDefinition entity = (EntityDefinition) sessionContext.getModelDefinition(modelName);
      Optional<TypedField<?, ?>> idFieldOptional = entity.findIdField();
      Object id = null;
      if (idFieldOptional.isPresent()) {
        id = processedData.get(idFieldOptional.get().getName());
        // 将生成的ID放回到原始的data map中
        data.put(idFieldOptional.get().getName(), id);
      }

      // 处理关联关系
      insertRelatedRecords(modelName, data, id);
    }
  }

  private void assignAutoIncrementId(String modelName, Map<String, Object> record) {
    String sequenceName = modelName + "_seq";
    try {
      schemaService.createSequence(sequenceName, 1, 1);
    } catch (Exception ignored) {
    }
    long sequenceNextVal = schemaService.getSequenceNextVal(sequenceName);
    EntityDefinition entity = (EntityDefinition) schemaService.getModel(modelName);
    TypedField<?, ?> idField = entity.findIdField().orElseThrow();
    record.put(idField.getName(), sequenceNextVal);
  }

  @Override
  public int updateById(String modelName, Map<String, Object> data, Object id) {

    Map<String, Object> processedData = generateFieldValues(modelName, data, true);

    String collectionName = getCollectionName(modelName);
    EntityDefinition entity = (EntityDefinition) sessionContext.getModelDefinition(modelName);
    TypedField<?, ?> idField = entity.findIdField().orElseThrow();
    UpdateResult result = mongoDatabase.getCollection(collectionName, Map.class).updateOne(Filters.eq(idField.getName(), id), new Document("$set", new Document(processedData)));
    return (int) result.getModifiedCount();
  }

  @Override
  public int update(String modelName, Map<String, Object> data, String filter) {
    Map<String, Object> processedData = generateFieldValues(modelName, data, true);
    String collectionName = getCollectionName(modelName);
    String mongoCondition = builder.getMongoCondition(filter);
    Document mongoFilter = Document.parse(mongoCondition);
    UpdateResult result = mongoDatabase.getCollection(collectionName).updateMany(mongoFilter, new Document("$set", new Document(processedData)));
    return (int) result.getModifiedCount();
  }

  @Override
  public int deleteById(String modelName, Object id) {
    String collectionName = getCollectionName(modelName);
    EntityDefinition entity = (EntityDefinition) sessionContext.getModelDefinition(modelName);
    TypedField<?, ?> idField = entity.findIdField().orElseThrow();
    return (int) mongoDatabase.getCollection(collectionName)
      .deleteMany(Filters.eq(idField.getName(), id)).getDeletedCount();
  }

  @Override
  public int delete(String modelName, String filter) {
    String collectionName = getCollectionName(modelName);
    String mongoCondition = builder.getMongoCondition(filter);
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
  public Map<String, Object> findById(String modelName, Object id, boolean nestedQuery) {
    String collectionName = getCollectionName(modelName);
    EntityDefinition entity = (EntityDefinition) sessionContext.getModelDefinition(modelName);
    TypedField<?, ?> idField = entity.findIdField().orElseThrow();

    Map dataMap = mongoDatabase.getCollection(collectionName, Map.class)
      .find(Filters.eq(idField.getName(), id))
      .first();
    if (nestedQuery && dataMap != null) {
      nestedQuery(List.of(dataMap), this::queryAsMapList, (ModelDefinition) sessionContext.getModelDefinition(modelName),
        null, sessionContext.getNestedQueryMaxDepth());
    }
    return dataMap;
  }

  @Override
  public List<Map<String, Object>> find(String modelName, Query query) {
    List<Map<String, Object>> mapList = queryAsMapList(modelName, query);
    if (query.isNestedEnabled()) {
      nestedQuery(mapList, this::queryAsMapList, (ModelDefinition) sessionContext.getModelDefinition(modelName), query, sessionContext.getNestedQueryMaxDepth());
    }
    return mapList;
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> findByNativeQuery(String modelName, Map<String, Object> params) {
    NativeQueryDefinition model = (NativeQueryDefinition) sessionContext.getModelDefinition(modelName);
    return (List<Map<String, Object>>) executeNativeStatement(model.getStatement(), params);
  }

  @Override
  public Object executeNativeStatement(String statement, Map<String, Object> params) {
    String json = StringHelper.simpleRenderTemplate(statement, params);
    Document command = Document.parse(json);
    Document result = mongoDatabase.runCommand(command);

    // 判断是否为查询操作
    boolean isQuery = command.containsKey("find") ||
                      command.containsKey("aggregate") ||
                      command.containsKey("count") ||
                      command.containsKey("distinct");

    if (isQuery) {
      // 查询操作
      List<?> list = (List<?>) ((Map<?, ?>) result.get("cursor")).get("firstBatch");
      return list;
    } else {
      // 更新操作
      if (result.containsKey("modifiedCount")) {
        return result.getInteger("modifiedCount");
      } else if (result.containsKey("matchedCount")) {
        return result.getInteger("matchedCount");
      } else if (result.containsKey("n")) {
        return result.getInteger("n");
      }
      return 0;
    }
  }

  @SuppressWarnings({"rawtypes"})
  private List<Map<String, Object>> queryAsMapList(String modelName, Query query) {
    List<Document> pipeline = builder.createPipeline(modelName, query);

    String collectionName = getCollectionName(modelName);
    List list = mongoDatabase.getCollection(collectionName, Map.class)
      .aggregate(pipeline)
      .into(new ArrayList<>());
    return list;
  }

  @Override
  public long count(String modelName, Query query) {
    String collectionName = getCollectionName(modelName);
    List<Document> pipeline = builder.createPipeline(modelName, query);
    ArrayList<Document> count = new ArrayList<>(pipeline);
    count.add(new Document("$count", "total"));
    Number total = mongoDatabase.getCollection(collectionName)
      .aggregate(count).map(map -> map.get("total", Number.class))
      .first();
    return total != null ? total.longValue() : 0;
  }

  @Override
  public DataService getDataService() {
    return this;
  }
}
