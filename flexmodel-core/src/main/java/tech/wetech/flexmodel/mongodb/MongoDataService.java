package tech.wetech.flexmodel.mongodb;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import tech.wetech.flexmodel.model.EntityDefinition;
import tech.wetech.flexmodel.model.ModelDefinition;
import tech.wetech.flexmodel.model.field.GeneratedValue;
import tech.wetech.flexmodel.model.field.TypedField;
import tech.wetech.flexmodel.query.Query;
import tech.wetech.flexmodel.reflect.ReflectionUtils;
import tech.wetech.flexmodel.service.BaseService;
import tech.wetech.flexmodel.service.DataService;
import tech.wetech.flexmodel.service.SchemaService;

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
  public int insert(String modelName, Object objR) {
    log.debug("Starting insert method for model: {}, data: {}", modelName, objR);
    long startTime = System.currentTimeMillis();

    Map<String, Object> data = null;
    Map<String, Object> processedData = null;

    try {
      data = ReflectionUtils.toClassBean(sessionContext.getJsonObjectConverter(), objR, Map.class);
      log.debug("Data operation: INSERT on model: {} with data: {}", modelName, data);

      processedData = generateValue(modelName, data, false);
      log.debug("Generated values for insert: {}", processedData);

      Map<String, Object> record = ReflectionUtils.toClassBean(sessionContext.getJsonObjectConverter(), processedData, Map.class);
      EntityDefinition entity = (EntityDefinition) sessionContext.getModel(modelName);
      TypedField<?, ?> idField = entity.findIdField().orElseThrow();

      if (GeneratedValue.AUTO_INCREMENT.equals(idField.getDefaultValue())) {
        log.debug("Setting auto-increment ID for model: {}", modelName);
        setId(modelName, record);
      }

      String collectionName = getCollectionName(modelName);
      log.debug("Inserting into MongoDB collection: {}", collectionName);

      InsertOneResult result = mongoDatabase.getCollection(collectionName, Map.class).insertOne(record);
      int insertCount = result.wasAcknowledged() ? 1 : 0;

      long duration = System.currentTimeMillis() - startTime;
      if (duration > 1000) {
        log.warn("Slow operation detected: MongoDB insert took {}ms for model: {}", duration, modelName);
      } else {
        log.debug("Operation performance: MongoDB insert took {}ms for model: {}", duration, modelName);
      }
      log.debug("Data operation result: INSERT on model: {} -> {}", modelName, insertCount);
      log.debug("Method completed successfully: insert -> {}", insertCount);

      return insertCount;
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("Method failed: insert - Failed to insert data for model: {}", modelName, e);
      log.warn("Operation performance: MongoDB insert (failed) took {}ms for model: {}", duration, modelName);
      throw e;
    } finally {
      // 获取生成的ID（如果有的话）
      if (data != null && processedData != null) {
        try {
          EntityDefinition entity = (EntityDefinition) sessionContext.getModel(modelName);
          Optional<TypedField<?, ?>> idFieldOptional = entity.findIdField();
          Object id = null;
          if (idFieldOptional.isPresent()) {
            id = processedData.get(idFieldOptional.get().getName());
            // 将生成的ID放回到原始的data map中
            data.put(idFieldOptional.get().getName(), id);
            log.debug("Generated ID for model {}: {}", modelName, id);
          }

          // 处理关联关系
          insertRelationRecord(modelName, data, id);
        } catch (Exception e) {
          log.error("Failed to process relations after insert", e);
        }
      }
    }
  }

  private void setId(String modelName, Map<String, Object> record) {
    log.debug("Starting setId method for model: {}", modelName);

    try {
      String sequenceName = modelName + "_seq";
      log.debug("Creating sequence: {}", sequenceName);

      try {
        schemaService.createSequence(sequenceName, 1, 1);
        log.debug("Sequence created successfully: {}", sequenceName);
      } catch (Exception ignored) {
        log.debug("Sequence already exists: {}", sequenceName);
      }

      long sequenceNextVal = schemaService.getSequenceNextVal(sequenceName);
      EntityDefinition entity = (EntityDefinition) schemaService.getModel(modelName);
      TypedField<?, ?> idField = entity.findIdField().orElseThrow();
      record.put(idField.getName(), sequenceNextVal);

      log.debug("Set auto-increment ID: {} = {}", idField.getName(), sequenceNextVal);
      log.debug("Method completed successfully: setId -> {}", sequenceNextVal);
    } catch (Exception e) {
      log.error("Method failed: setId - Failed to set ID for model: {}", modelName, e);
      throw e;
    }
  }

  @Override
  public int updateById(String modelName, Object objR, Object id) {
    log.debug("Starting updateById method for model: {}, id: {}", modelName, id);
    long startTime = System.currentTimeMillis();

    try {
      Map<String, Object> data = ReflectionUtils.toClassBean(sessionContext.getJsonObjectConverter(), objR, Map.class);
      log.debug("Data operation: UPDATE_BY_ID on model: {} with data: {}", modelName, data);

      Map<String, Object> processedData = generateValue(modelName, data, true);
      log.debug("Generated values for update: {}", processedData);

      Map<String, Object> record = ReflectionUtils.toClassBean(sessionContext.getJsonObjectConverter(), processedData, Map.class);
      String collectionName = getCollectionName(modelName);
      EntityDefinition entity = (EntityDefinition) sessionContext.getModel(modelName);
      TypedField<?, ?> idField = entity.findIdField().orElseThrow();

      log.debug("Updating MongoDB document: collection={}, id={}", collectionName, id);

      UpdateResult result = mongoDatabase.getCollection(collectionName, Map.class)
        .updateOne(Filters.eq(idField.getName(), id), new Document("$set", new Document(record)));

      int updateCount = (int) result.getModifiedCount();

      long duration = System.currentTimeMillis() - startTime;
      if (duration > 1000) {
        log.warn("Slow operation detected: MongoDB updateById took {}ms for model: {}, id: {}", duration, modelName, id);
      } else {
        log.debug("Operation performance: MongoDB updateById took {}ms for model: {}, id: {}", duration, modelName, id);
      }
      log.debug("Data operation result: UPDATE_BY_ID on model: {} -> {}", modelName, updateCount);
      log.debug("Method completed successfully: updateById -> {}", updateCount);

      return updateCount;
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("Method failed: updateById - Failed to update data for model: {}, id: {}", modelName, id, e);
      log.warn("Operation performance: MongoDB updateById (failed) took {}ms for model: {}, id: {}", duration, modelName, id);
      throw e;
    }
  }

  @Override
  public int update(String modelName, Object objR, String filter) {
    log.debug("Starting update method for model: {}, filter: {}", modelName, filter);
    long startTime = System.currentTimeMillis();

    try {
      Map<String, Object> data = ReflectionUtils.toClassBean(sessionContext.getJsonObjectConverter(), objR, Map.class);
      log.debug("Data operation: UPDATE on model: {} with data: {}", modelName, data);

      Map<String, Object> processedData = generateValue(modelName, data, true);
      log.debug("Generated values for update: {}", processedData);

      Map<String, Object> record = ReflectionUtils.toClassBean(sessionContext.getJsonObjectConverter(), processedData, Map.class);
      String collectionName = getCollectionName(modelName);
      String mongoCondition = builder.getMongoCondition(filter);
      Document mongoFilter = Document.parse(mongoCondition);

      log.debug("Updating MongoDB documents: collection={}, filter={}", collectionName, mongoCondition);

      UpdateResult result = mongoDatabase.getCollection(collectionName)
        .updateMany(mongoFilter, new Document("$set", new Document(record)));

      int updateCount = (int) result.getModifiedCount();

      long duration = System.currentTimeMillis() - startTime;
      if (duration > 1000) {
        log.warn("Slow operation detected: MongoDB update took {}ms for model: {}, filter: {}", duration, modelName, filter);
      } else {
        log.debug("Operation performance: MongoDB update took {}ms for model: {}, filter: {}", duration, modelName, filter);
      }
      log.debug("Data operation result: UPDATE on model: {} -> {}", modelName, updateCount);
      log.debug("Method completed successfully: update -> {}", updateCount);

      return updateCount;
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("Method failed: update - Failed to update data for model: {}, filter: {}", modelName, filter, e);
      log.warn("Operation performance: MongoDB update (failed) took {}ms for model: {}, filter: {}", duration, modelName, filter);
      throw e;
    }
  }

  @Override
  public int deleteById(String modelName, Object id) {
    log.debug("Starting deleteById method for model: {}, id: {}", modelName, id);
    long startTime = System.currentTimeMillis();

    try {
      String collectionName = getCollectionName(modelName);
      EntityDefinition entity = (EntityDefinition) sessionContext.getModel(modelName);
      TypedField<?, ?> idField = entity.findIdField().orElseThrow();

      log.debug("Deleting MongoDB document: collection={}, id={}", collectionName, id);

      long deleteCount = mongoDatabase.getCollection(collectionName)
        .deleteMany(Filters.eq(idField.getName(), id)).getDeletedCount();

      int result = (int) deleteCount;

      long duration = System.currentTimeMillis() - startTime;
      if (duration > 1000) {
        log.warn("Slow operation detected: MongoDB deleteById took {}ms for model: {}, id: {}", duration, modelName, id);
      } else {
        log.debug("Operation performance: MongoDB deleteById took {}ms for model: {}, id: {}", duration, modelName, id);
      }
      log.debug("Data operation result: DELETE_BY_ID on model: {} -> {}", modelName, result);
      log.debug("Method completed successfully: deleteById -> {}", result);

      return result;
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("Method failed: deleteById - Failed to delete data for model: {}, id: {}", modelName, id, e);
      log.warn("Operation performance: MongoDB deleteById (failed) took {}ms for model: {}, id: {}", duration, modelName, id);
      throw e;
    }
  }

  @Override
  public int delete(String modelName, String filter) {
    log.debug("Starting delete method for model: {}, filter: {}", modelName, filter);
    long startTime = System.currentTimeMillis();

    try {
      String collectionName = getCollectionName(modelName);
      String mongoCondition = builder.getMongoCondition(filter);

      log.debug("Deleting MongoDB documents: collection={}, filter={}", collectionName, mongoCondition);

      long deleteCount = mongoDatabase.getCollection(collectionName)
        .deleteMany(Document.parse(mongoCondition)).getDeletedCount();

      int result = (int) deleteCount;

      long duration = System.currentTimeMillis() - startTime;
      if (duration > 1000) {
        log.warn("Slow operation detected: MongoDB delete took {}ms for model: {}, filter: {}", duration, modelName, filter);
      } else {
        log.debug("Operation performance: MongoDB delete took {}ms for model: {}, filter: {}", duration, modelName, filter);
      }
      log.debug("Data operation result: DELETE on model: {} -> {}", modelName, result);
      log.debug("Method completed successfully: delete -> {}", result);

      return result;
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("Method failed: delete - Failed to delete data for model: {}, filter: {}", modelName, filter, e);
      log.warn("Operation performance: MongoDB delete (failed) took {}ms for model: {}, filter: {}", duration, modelName, filter);
      throw e;
    }
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
    log.debug("Starting findById method for model: {}, id: {}, resultType: {}, nestedQuery: {}", modelName, id, resultType, nestedQuery);
    long startTime = System.currentTimeMillis();

    try {
      String collectionName = getCollectionName(modelName);
      EntityDefinition entity = (EntityDefinition) sessionContext.getModel(modelName);
      TypedField<?, ?> idField = entity.findIdField().orElseThrow();

      log.debug("Finding document by ID: collection={}, id={}", collectionName, id);

      Map<String, Object> result = mongoDatabase.getCollection(collectionName, Map.class)
        .find(Filters.eq(idField.getName(), id)).first();

      if (result == null) {
        log.debug("No document found for ID: {}", id);
        long duration = System.currentTimeMillis() - startTime;
        if (duration > 1000) {
          log.warn("Slow operation detected: MongoDB findById took {}ms for model: {}, id: {} (not found)", duration, modelName, id);
        } else {
          log.debug("Operation performance: MongoDB findById took {}ms for model: {}, id: {} (not found)", duration, modelName, id);
        }
        log.debug("Method completed successfully: findById -> null");
        return null;
      }

      if (nestedQuery) {
        log.debug("Processing nested query for found document");
        nestedQuery(List.of(result), this::findMapList, entity, null, 3);
      }

      T convertedResult = sessionContext.getJsonObjectConverter().convertValue(result, resultType);

      long duration = System.currentTimeMillis() - startTime;
      if (duration > 1000) {
        log.warn("Slow operation detected: MongoDB findById took {}ms for model: {}, id: {}", duration, modelName, id);
      } else {
        log.debug("Operation performance: MongoDB findById took {}ms for model: {}, id: {}", duration, modelName, id);
      }
      log.debug("Data operation result: SELECT_BY_ID on model: {} -> {}", modelName, convertedResult);
      log.debug("Method completed successfully: findById -> {}", convertedResult);

      return convertedResult;
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("Method failed: findById - Failed to find data for model: {}, id: {}", modelName, id, e);
      log.warn("Operation performance: MongoDB findById (failed) took {}ms for model: {}, id: {}", duration, modelName, id);
      throw e;
    }
  }

  @Override
  public <T> List<T> find(String modelName, Query query, Class<T> resultType) {
    log.debug("Starting find method for model: {}, query: {}, resultType: {}", modelName, query, resultType);
    long startTime = System.currentTimeMillis();

    try {
      log.debug("Finding documents with query: {}", query);

      List<Map<String, Object>> resultList = findMapList(modelName, query);

      List<T> convertedResults = sessionContext.getJsonObjectConverter().convertValueList(resultList, resultType);

      long duration = System.currentTimeMillis() - startTime;
      if (duration > 1000) {
        log.warn("Slow operation detected: MongoDB find took {}ms for model: {}, results: {}", duration, modelName, convertedResults.size());
      } else {
        log.debug("Operation performance: MongoDB find took {}ms for model: {}, results: {}", duration, modelName, convertedResults.size());
      }
      log.debug("Data operation result: SELECT on model: {} -> {}", modelName, convertedResults.size());
      log.debug("Method completed successfully: find -> {}", convertedResults.size());

      return convertedResults;
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("Method failed: find - Failed to find data for model: {}", modelName, e);
      log.warn("Operation performance: MongoDB find (failed) took {}ms for model: {}", duration, modelName);
      throw e;
    }
  }

  @Override
  public <T> List<T> findByNativeQueryStatement(String statement, Object obj, Class<T> resultType) {
    log.debug("Starting findByNativeQueryStatement method for statement: {}, obj: {}, resultType: {}", statement, obj, resultType);
    long startTime = System.currentTimeMillis();

    try {
      log.debug("Executing native query statement: {}", statement);

      List resultList = mongoDatabase.getCollection("temp", Map.class)
        .find(Document.parse(statement)).into(new ArrayList<>());

      List<T> convertedResults = sessionContext.getJsonObjectConverter().convertValueList(resultList, resultType);

      long duration = System.currentTimeMillis() - startTime;
      if (duration > 1000) {
        log.warn("Slow operation detected: MongoDB native query took {}ms for results: {}", duration, convertedResults.size());
      } else {
        log.debug("Operation performance: MongoDB native query took {}ms for results: {}", duration, convertedResults.size());
      }
      log.debug("Data operation result: NATIVE_QUERY on temp -> {}", convertedResults.size());
      log.debug("Method completed successfully: findByNativeQueryStatement -> {}", convertedResults.size());

      return convertedResults;
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("Method failed: findByNativeQueryStatement - Failed to execute native query: {}", statement, e);
      log.warn("Operation performance: MongoDB native query (failed) took {}ms for statement: {}", duration, statement);
      throw e;
    }
  }

  @Override
  public <T> List<T> findByNativeQueryModel(String modelName, Object obj, Class<T> resultType) {
    log.debug("Starting findByNativeQueryModel method for model: {}, obj: {}, resultType: {}", modelName, obj, resultType);
    long startTime = System.currentTimeMillis();

    try {
      log.debug("Executing native query for model: {}", modelName);

      List resultList = mongoDatabase.getCollection(modelName, Map.class)
        .find(Document.parse(obj.toString())).into(new ArrayList<>());

      List<T> convertedResults = sessionContext.getJsonObjectConverter().convertValueList(resultList, resultType);

      long duration = System.currentTimeMillis() - startTime;
      if (duration > 1000) {
        log.warn("Slow operation detected: MongoDB native query model took {}ms for model: {}, results: {}", duration, modelName, convertedResults.size());
      } else {
        log.debug("Operation performance: MongoDB native query model took {}ms for model: {}, results: {}", duration, modelName, convertedResults.size());
      }
      log.debug("Data operation result: NATIVE_QUERY_MODEL on model: {} -> {}", modelName, convertedResults.size());
      log.debug("Method completed successfully: findByNativeQueryModel -> {}", convertedResults.size());

      return convertedResults;
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("Method failed: findByNativeQueryModel - Failed to execute native query for model: {}", modelName, e);
      log.warn("Operation performance: MongoDB native query model (failed) took {}ms for model: {}", duration, modelName);
      throw e;
    }
  }

  @SuppressWarnings({"rawtypes"})
  private List<Map<String, Object>> findMapList(String modelName, Query query) {
    log.debug("Starting findMapList method for model: {}, query: {}", modelName, query);
    long startTime = System.currentTimeMillis();

    try {
      String collectionName = getCollectionName(modelName);
      log.debug("Finding documents in collection: {}", collectionName);

      List<Document> pipeline = builder.createPipeline(modelName, query);
      List resultList = mongoDatabase.getCollection(collectionName, Map.class)
        .aggregate(pipeline).into(new ArrayList<>());

      if (query != null) {
        log.debug("Processing nested query for {} documents", resultList.size());
        nestedQuery(resultList, this::findMapList, (ModelDefinition) sessionContext.getModel(modelName), query, 3);
      }

      long duration = System.currentTimeMillis() - startTime;
      if (duration > 1000) {
        log.warn("Slow operation detected: MongoDB findMapList took {}ms for model: {}, results: {}", duration, modelName, resultList.size());
      } else {
        log.debug("Operation performance: MongoDB findMapList took {}ms for model: {}, results: {}", duration, modelName, resultList.size());
      }
      log.debug("Method completed successfully: findMapList -> {}", resultList.size());

      return resultList;
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("Method failed: findMapList - Failed to find map list for model: {}", modelName, e);
      log.warn("Operation performance: MongoDB findMapList (failed) took {}ms for model: {}", duration, modelName);
      throw e;
    }
  }

  @Override
  public long count(String modelName, Query query) {
    log.debug("Starting count method for model: {}, query: {}", modelName, query);
    long startTime = System.currentTimeMillis();

    try {
      String collectionName = getCollectionName(modelName);
      log.debug("Counting documents in collection: {}", collectionName);

      List<Document> pipeline = builder.createPipeline(modelName, query);
      ArrayList<Document> countPipeline = new ArrayList<>(pipeline);
      countPipeline.add(new Document("$count", "total"));
      Number total = mongoDatabase.getCollection(collectionName)
        .aggregate(countPipeline).map(map -> map.get("total", Number.class)).first();
      long count = total != null ? total.longValue() : 0;

      long duration = System.currentTimeMillis() - startTime;
      if (duration > 1000) {
        log.warn("Slow operation detected: MongoDB count took {}ms for model: {}, count: {}", duration, modelName, count);
      } else {
        log.debug("Operation performance: MongoDB count took {}ms for model: {}, count: {}", duration, modelName, count);
      }
      log.debug("Data operation result: COUNT on model: {} -> {}", modelName, count);
      log.debug("Method completed successfully: count -> {}", count);

      return count;
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("Method failed: count - Failed to count documents for model: {}", modelName, e);
      log.warn("Operation performance: MongoDB count (failed) took {}ms for model: {}", duration, modelName);
      throw e;
    }
  }

  @Override
  public DataService getDataService() {
    return this;
  }
}
