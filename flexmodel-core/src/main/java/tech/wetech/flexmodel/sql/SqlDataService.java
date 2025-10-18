package tech.wetech.flexmodel.sql;

import tech.wetech.flexmodel.ExpressionCalculator;
import tech.wetech.flexmodel.ExpressionCalculatorException;
import tech.wetech.flexmodel.model.EntityDefinition;
import tech.wetech.flexmodel.model.ModelDefinition;
import tech.wetech.flexmodel.model.NativeQueryDefinition;
import tech.wetech.flexmodel.model.field.Field;
import tech.wetech.flexmodel.model.field.RelationField;
import tech.wetech.flexmodel.model.field.TypedField;
import tech.wetech.flexmodel.query.Query;
import tech.wetech.flexmodel.reflect.LazyObjProxy;
import tech.wetech.flexmodel.reflect.ReflectionUtils;
import tech.wetech.flexmodel.service.BaseService;
import tech.wetech.flexmodel.service.DataService;
import tech.wetech.flexmodel.sql.SqlStatementBuilder.Pair;
import tech.wetech.flexmodel.sql.dialect.SqlDialect;
import tech.wetech.flexmodel.sql.type.SqlResultHandler;
import tech.wetech.flexmodel.sql.type.SqlTypeHandler;
import tech.wetech.flexmodel.sql.type.UnknownSqlTypeHandler;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author cjbi
 */
public class SqlDataService extends BaseService implements DataService {

  private final SqlExecutor sqlExecutor;
  private final SqlDialect sqlDialect;
  private final ExpressionCalculator<SqlClauseResult> sqlExpressionCalculator;
  private final Map<String, SqlTypeHandler<?>> typeHandlerMap;
  private final SqlStatementBuilder builder;
  private final SqlContext sessionContext;

  public SqlDataService(SqlContext sessionContext) {
    super(sessionContext);
    this.sqlExecutor = sessionContext.getJdbcOperations();
    this.sqlDialect = sessionContext.getSqlDialect();
    this.sqlExpressionCalculator = sessionContext.getConditionCalculator();
    this.typeHandlerMap = sessionContext.getTypeHandlerMap();
    this.sessionContext = sessionContext;
    this.builder = new SqlStatementBuilder(sessionContext);
  }

  @Override
  public int insert(String modelName, Object objR) {
    log.debug("Starting SQL insert for model: {}", modelName);
    long startTime = System.currentTimeMillis();

    Map<String, Object> data = ReflectionUtils.toClassBean(sessionContext.getJsonObjectConverter(), objR, Map.class);
    Map<String, Object> processedData = generateFieldValues(modelName, data, false);

    try {
      Map<String, Object> record = ReflectionUtils.toClassBean(sessionContext.getJsonObjectConverter(), processedData, Map.class);
      String sql = getInsertSqlString(modelName, record);
      log.debug("Generated INSERT SQL: {}", sql);

      EntityDefinition entity = (EntityDefinition) sessionContext.getModelDefinition(modelName);
      Optional<TypedField<?, ?>> idFieldOptional = entity.findIdField();
      int rows;
      if (idFieldOptional.isPresent()) {
        TypedField<?, ?> idField = idFieldOptional.get();
        if (record.get(idField.getName()) != null) {
          // ID already provided in the record, just insert
          rows = sqlExecutor.update(sql, record);
        } else {
          // Auto-generate ID and put it back into the record
          if (sqlDialect.useFirstGeneratedId()) {
            rows = sqlExecutor.updateAndReturnFirstGeneratedKeys(sql, record, generatedId ->
              record.put(idField.getName(), generatedId));
          } else {
            rows = sqlExecutor.updateAndReturnGeneratedKeys(sql, record,
              new String[]{sqlDialect.getGeneratedKeyName(idField.getName())}, keys ->
                record.put(idField.getName(), keys.getFirst()));
          }
        }
        // 返回ID值
        ReflectionUtils.setFieldValue(objR, idField.getName(), record.get(idField.getName()));
      } else {
        rows = sqlExecutor.update(sql, record);
      }

      long duration = System.currentTimeMillis() - startTime;
      log.debug("SQL insert completed for model: {} in {}ms, affected rows: {}", modelName, duration, rows);
      return rows;
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("SQL insert failed for model: {} after {}ms", modelName, duration, e);
      throw e;
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

  private String getInsertSqlString(String modelName, Map<String, Object> record) {
    String physicalTableName = toPhysicalTablenameQuoteString(modelName);
    EntityDefinition entity = (EntityDefinition) sessionContext.getModelDefinition(modelName);
    Optional<TypedField<?, ?>> idFieldOptional = entity.findIdField();
    StringJoiner columns = new StringJoiner(", ", "(", ")");
    StringJoiner values = new StringJoiner(", ", "(", ")");
    record.forEach((key, value) -> {
      if (idFieldOptional.isPresent() && key.equals(idFieldOptional.get().getName())) {
        if (record.get(key) != null) {
          columns.add(sqlDialect.quoteIdentifier(key));
          values.add(":" + key);
        }
      } else {
        columns.add(sqlDialect.quoteIdentifier(key));
        values.add(":" + key);
      }
    });
    return "insert into " +
           physicalTableName +
           columns +
           " values " +
           values;
  }

  @Override
  public int updateById(String modelName, Object objR, Object id) {
    log.debug("Starting SQL updateById for model: {}, id: {}", modelName, id);
    long startTime = System.currentTimeMillis();

    try {
      Map<String, Object> data = ReflectionUtils.toClassBean(sessionContext.getJsonObjectConverter(), objR, Map.class);
      Map<String, Object> processedData = generateFieldValues(modelName, data, true);

      Map<String, Object> record = ReflectionUtils.toClassBean(sessionContext.getJsonObjectConverter(), processedData, Map.class);
      String physicalTableName = toPhysicalTablenameQuoteString(modelName);
      EntityDefinition entity = (EntityDefinition) sessionContext.getModelDefinition(modelName);
      TypedField<?, ?> idField = entity.findIdField().orElseThrow();

      StringBuilder sql = new StringBuilder("update ")
        .append(physicalTableName)
        .append(" set ");
      StringJoiner assignment = new StringJoiner(", ");

      record.keySet().stream()
        .filter(col -> !col.equals(idField.getName()) && (data.containsKey(col) || processedData.get(col) != null))
        .forEach(col -> assignment.add(sqlDialect.quoteIdentifier(col) + "=:" + col));

      sql.append(assignment);

      sql.append(" where (")
        .append(sqlDialect.quoteIdentifier(idField.getName()))
        .append("=:")
        .append(idField.getName())
        .append(")");

      Map<String, Object> params = new HashMap<>(processedData);
      params.put(idField.getName(), id);

      log.debug("Generated UPDATE SQL: {}", sql);
      int result = sqlExecutor.update(sql.toString(), params);

      long duration = System.currentTimeMillis() - startTime;
      log.debug("SQL updateById completed for model: {} in {}ms, affected rows: {}", modelName, duration, result);
      return result;
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("SQL updateById failed for model: {}, id: {} after {}ms", modelName, id, duration, e);
      throw e;
    }
  }

  @Override
  public int update(String modelName, Object objR, String filter) {
    log.debug("Starting SQL update for model: {}, filter: {}", modelName, filter);
    long startTime = System.currentTimeMillis();

    try {
      Map<String, Object> record = ReflectionUtils.toClassBean(sessionContext.getJsonObjectConverter(), objR, Map.class);
      Map<String, Object> processedData = generateFieldValues(modelName, record, true);

      String physicalTableName = toPhysicalTablenameQuoteString(modelName);
      EntityDefinition entity = (EntityDefinition) sessionContext.getModelDefinition(modelName);
      TypedField<?, ?> idField = entity.findIdField().orElseThrow();
      SqlClauseResult sqlResult = getSqlCauseResult(filter);

      StringBuilder sql = new StringBuilder("update ")
        .append(physicalTableName)
        .append(" set ");
      StringJoiner assignment = new StringJoiner(", ");
      processedData.keySet().stream()
        .filter(col -> !col.equals(idField.getName()) && (record.containsKey(col) || processedData.get(col) != null))
        .forEach(col -> assignment.add(sqlDialect.quoteIdentifier(col) + "=:" + col));
      sql.append(assignment);

      sql.append(" where ")
        .append(" (").append(sqlResult.sqlClause()).append(")");

      Map<String, Object> params = new HashMap<>(processedData);
      params.putAll(sqlResult.args());

      log.debug("Generated UPDATE SQL: {}", sql);
      int result = sqlExecutor.update(sql.toString(), params);

      long duration = System.currentTimeMillis() - startTime;
      log.debug("SQL update completed for model: {} in {}ms, affected rows: {}", modelName, duration, result);
      return result;
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("SQL update failed for model: {}, filter: {} after {}ms", modelName, filter, duration, e);
      throw e;
    }
  }

  @Override
  public <T> T findById(String modelName, Object id, Class<T> resultType, boolean nestedQuery) {
    log.debug("Starting SQL findById for model: {}, id: {}, nestedQuery: {}", modelName, id, nestedQuery);
    long startTime = System.currentTimeMillis();

    try {
      String physicalTableName = toPhysicalTablenameQuoteString(modelName);
      EntityDefinition entity = (EntityDefinition) sessionContext.getModelDefinition(modelName);
      TypedField<?, ?> idField = entity.findIdField().orElseThrow();
      String columnsString = entity.getFields().stream()
        .filter(f -> !(f instanceof RelationField))
        .map(field -> sqlDialect.quoteIdentifier(field.getName()))
        .collect(Collectors.joining(", "));

      String sql = " select " +
                   columnsString +
                   " from " +
                   physicalTableName +
                   " " +
                   " where (" + sqlDialect.quoteIdentifier(idField.getName()) + "= :id)";

      log.debug("Generated SELECT SQL: {}", sql);
      Map<String, Object> dataMap = sqlExecutor.queryForObject(sql, Map.of("id", id), getSqlResultHandler(entity, null, Map.class));

      if (dataMap == null) {
        log.debug("No record found for model: {}, id: {}", modelName, id);
        return null;
      }

      if (nestedQuery && dataMap != null) {
        nestedQuery(List.of(dataMap), this::findMapList, (ModelDefinition) sessionContext.getModelDefinition(modelName), null, sessionContext.getNestedQueryMaxDepth());
      }

      T result = ReflectionUtils.toClassBean(sessionContext.getJsonObjectConverter(), dataMap, resultType);
      T finalResult = LazyObjProxy.createProxy(result, modelName, sessionContext);

      long duration = System.currentTimeMillis() - startTime;
      log.debug("SQL findById completed for model: {} in {}ms", modelName, duration);
      return finalResult;
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("SQL findById failed for model: {}, id: {} after {}ms", modelName, id, duration, e);
      throw e;
    }
  }

  @Override
  @SuppressWarnings("all")
  public <T> List<T> find(String modelName, Query query, Class<T> resultType) {
    log.debug("Starting SQL find for model: {}", modelName);
    long startTime = System.currentTimeMillis();

    try {
      Pair<String, Map<String, Object>> pair = builder.toQuerySqlWithPrepared(modelName, query);
      log.debug("Generated SELECT SQL: {}", pair.first());

      List mapList = sqlExecutor.queryForList(pair.first(), pair.second(), getSqlResultHandler((ModelDefinition) sessionContext.getModelDefinition(modelName), query, Map.class));
      if (query.isNestedEnabled()) {
        nestedQuery(mapList, this::findMapList, (ModelDefinition) sessionContext.getModelDefinition(modelName), query, sessionContext.getNestedQueryMaxDepth());
      }
      List<T> results = ReflectionUtils.toClassBeanList(sessionContext.getJsonObjectConverter(), mapList, resultType);
      List<T> finalResults = LazyObjProxy.createProxyList(results, modelName, sessionContext);

      long duration = System.currentTimeMillis() - startTime;
      log.debug("SQL find completed for model: {} in {}ms, results: {}", modelName, duration, finalResults.size());
      return finalResults;
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("SQL find failed for model: {} after {}ms", modelName, duration, e);
      throw e;
    }
  }


  @Override
  public <T> List<T> findByNativeQuery(String modelName, Object params, Class<T> resultType) {
    log.debug("Starting SQL native query model: {}", modelName);
    long startTime = System.currentTimeMillis();

    try {
      NativeQueryDefinition model = (NativeQueryDefinition) sessionContext.getModelDefinition(modelName);
      String statement = model.getStatement();
      List<?> list = (List<?>) executeNativeStatement(statement, params);

      List<T> results = ReflectionUtils.toClassBeanList(sessionContext.getJsonObjectConverter(), list, resultType);

      long duration = System.currentTimeMillis() - startTime;
      log.debug("SQL native query model completed for {} in {}ms, results: {}", modelName, duration, results.size());
      return results;
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("SQL native query model failed for {} after {}ms", modelName, duration, e);
      throw e;
    }
  }

  @Override
  public Object executeNativeStatement(String statement, Object objR) {
    log.debug("Starting SQL native execute: {}", statement);
    long startTime = System.currentTimeMillis();

    try {
      Map<String, Object> params = ReflectionUtils.toClassBean(sessionContext.getJsonObjectConverter(), objR, Map.class);
      String processedStatement = StringHelper.replacePlaceholder(statement);

      // 判断 SQL 语句类型
      String trimmedStatement = processedStatement.trim().toUpperCase();
      boolean isQuery = trimmedStatement.startsWith("SELECT");
      boolean isUpdate = trimmedStatement.startsWith("UPDATE")
                         || trimmedStatement.startsWith("DELETE");
      Object result;
      if (isQuery) {
        // 执行查询操作
        List<Map<String, Object>> queryResult = sqlExecutor.queryForList(processedStatement, params);
        result = queryResult;
        log.debug("SQL native query completed in {}ms, results: {}",
          System.currentTimeMillis() - startTime, queryResult.size());
      } else if (isUpdate) {
        // 执行更新操作
        int affectedRows = sqlExecutor.update(processedStatement, params);
        result = affectedRows;
        log.debug("SQL native update completed in {}ms, affected rows: {}",
          System.currentTimeMillis() - startTime, affectedRows);
      } else {
        // 不支持的 SQL 语句类型
        throw new IllegalArgumentException("Unsupported SQL statement: " + statement);
      }

      long duration = System.currentTimeMillis() - startTime;
      log.debug("SQL native execute completed in {}ms", duration);
      return result;
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("SQL native execute failed after {}ms", duration, e);
      throw e;
    }
  }

  @SuppressWarnings("all")
  private List<Map<String, Object>> findMapList(String modelName, Query query) {
    log.debug("Starting SQL findMapList for model: {}", modelName);
    long startTime = System.currentTimeMillis();

    try {
      Pair<String, Map<String, Object>> pair = builder.toQuerySqlWithPrepared(modelName, query);
      List list = sqlExecutor.queryForList(pair.first(), pair.second(), getSqlResultHandler((ModelDefinition) sessionContext.getModelDefinition(modelName), query, Map.class));

      long duration = System.currentTimeMillis() - startTime;
      log.debug("SQL findMapList completed for model: {} in {}ms, results: {}", modelName, duration, list.size());
      return list;
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("SQL findMapList failed for model: {} after {}ms", modelName, duration, e);
      throw e;
    }
  }

  @Override
  public long count(String modelName, Query query) {
    log.debug("Starting SQL count for model: {}", modelName);
    long startTime = System.currentTimeMillis();

    try {
      Pair<String, Map<String, Object>> pair = builder.toQuerySqlWithPrepared(modelName, query);
      String sql = "select count(*) from(" + pair.first() + ") tmp_count";
      log.debug("Generated COUNT SQL: {}", sql);

      long result = sqlExecutor.queryForScalar(sql, pair.second(), Long.class);

      long duration = System.currentTimeMillis() - startTime;
      log.debug("SQL count completed for model: {} in {}ms, count: {}", modelName, duration, result);
      return result;
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("SQL count failed for model: {} after {}ms", modelName, duration, e);
      throw e;
    }
  }

  @Override
  public int deleteById(String modelName, Object id) {
    log.debug("Starting SQL deleteById for model: {}, id: {}", modelName, id);
    long startTime = System.currentTimeMillis();

    try {
      String physicalTableName = toPhysicalTablenameQuoteString(modelName);
      EntityDefinition entity = (EntityDefinition) sessionContext.getModelDefinition(modelName);
      TypedField<?, ?> idField = entity.findIdField().orElseThrow();
      String sql = "delete from " +
                   physicalTableName +
                   " where (" + sqlDialect.quoteIdentifier(idField.getName()) + "=:" + idField.getName() + ")";

      log.debug("Generated DELETE SQL: {}", sql);
      int result = sqlExecutor.update(sql, Map.of(idField.getName(), id));

      long duration = System.currentTimeMillis() - startTime;
      log.debug("SQL deleteById completed for model: {} in {}ms, affected rows: {}", modelName, duration, result);
      return result;
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("SQL deleteById failed for model: {}, id: {} after {}ms", modelName, id, duration, e);
      throw e;
    }
  }

  @Override
  public int delete(String modelName, String filter) {
    log.debug("Starting SQL delete for model: {}, filter: {}", modelName, filter);
    long startTime = System.currentTimeMillis();

    try {
      SqlClauseResult sqlResult = getSqlCauseResult(filter);
      String physicalTableName = toPhysicalTablenameQuoteString(modelName);
      String sql = "delete from " +
                   physicalTableName +
                   " where (" +
                   sqlResult.sqlClause() + ")";

      log.debug("Generated DELETE SQL: {}", sql);
      int result = sqlExecutor.update(sql, sqlResult.args());

      long duration = System.currentTimeMillis() - startTime;
      log.debug("SQL delete completed for model: {} in {}ms, affected rows: {}", modelName, duration, result);
      return result;
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("SQL delete failed for model: {}, filter: {} after {}ms", modelName, filter, duration, e);
      throw e;
    }
  }

  private SqlClauseResult getSqlCauseResult(String condition) {
    try {
      return sqlExpressionCalculator.calculate(condition, null);
    } catch (ExpressionCalculatorException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public int deleteAll(String modelName) {
    String physicalTableName = toPhysicalTablenameQuoteString(modelName);
    return sqlExecutor.update("delete from " + physicalTableName);
  }

  /**
   * @param model
   * @param query      is nullable
   * @param resultType
   * @param <T>
   * @return
   */
  private <T> SqlResultHandler<T> getSqlResultHandler(ModelDefinition model, Query query, Class<T> resultType) {
    SqlResultHandler<T> sqlResultHandler = new SqlResultHandler<>(resultType);
    if (query == null || query.getProjection() == null || query.getProjection().getFields().isEmpty()) {
      for (Field field : model.getFields()) {
        if (field instanceof TypedField<?, ?> typedField) {
          sqlResultHandler.addSqlTypeHandler(field.getName(), typeHandlerMap.get(typedField.getType()), field);
        } else if (field instanceof Query.QueryField) {
          sqlResultHandler.addSqlTypeHandler(field.getName(), new UnknownSqlTypeHandler(), field);
        }
      }
    } else {
      Map<String, Query.QueryCall> fields = query.getProjection().getFields();
      for (Map.Entry<String, Query.QueryCall> entry : fields.entrySet()) {
        String key = entry.getKey();
        Query.QueryCall value = entry.getValue();
        sqlResultHandler.addSqlTypeHandler(key, new UnknownSqlTypeHandler(), null);
        if (value instanceof Query.QueryField queryField) {
          if (queryField.getAliasName() != null) {
            ModelDefinition queryModel = (ModelDefinition) sessionContext.getModelDefinition(queryField.getAliasName());
            Field field = queryModel != null
              ? queryModel.getField(queryField.getFieldName())
              : model.getField(queryField.getFieldName());
            if (field instanceof TypedField<?, ?> typedField) {
              sqlResultHandler.addSqlTypeHandler(key, sessionContext.getTypeHandler(typedField.getType()), null);
            } else {
              sqlResultHandler.addSqlTypeHandler(key, new UnknownSqlTypeHandler(), null);
            }
          } else {
            sqlResultHandler.addSqlTypeHandler(key, new UnknownSqlTypeHandler(), null);
          }
          Field field = queryField.getAliasName() != null
            ? ((ModelDefinition) sessionContext.getModelDefinition(queryField.getAliasName())).getField(queryField.getFieldName())
            : model.getField(queryField.getFieldName());
          if (field instanceof TypedField<?, ?> typedField) {
            sqlResultHandler.addSqlTypeHandler(key, sessionContext.getTypeHandler(typedField.getType()), field);
          } else {
            sqlResultHandler.addSqlTypeHandler(key, new UnknownSqlTypeHandler(), null);
          }
        } else {
          sqlResultHandler.addSqlTypeHandler(key, new UnknownSqlTypeHandler(), null);
        }
      }
    }
    return sqlResultHandler;
  }

  private String toPhysicalTablenameQuoteString(String name) {
    EntityDefinition entity = (EntityDefinition) sessionContext.getModelDefinition(name);
    if (entity == null) {
      return sqlDialect.quoteIdentifier(name);
    }
    return sqlDialect.quoteIdentifier(entity.getName());
  }

  @Override
  public DataService getDataService() {
    return this;
  }
}
