package tech.wetech.flexmodel.sql;

import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.sql.dialect.SqlDialect;
import tech.wetech.flexmodel.sql.type.SqlResultHandler;
import tech.wetech.flexmodel.sql.type.SqlTypeHandler;
import tech.wetech.flexmodel.sql.type.UnknownSqlTypeHandler;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author cjbi
 */
public class SqlDataOperations extends BaseSqlStatement implements DataOperations {

  private final String schemaName;
  private final SqlExecutor sqlExecutor;
  private final MappedModels mappedModels;
  private final SqlDialect sqlDialect;
  private final ExpressionCalculator<SqlClauseResult> sqlExpressionCalculator;
  private final Map<String, SqlTypeHandler<?>> typeHandlerMap;

  public SqlDataOperations(SqlContext sqlContext) {
    super(sqlContext);
    this.schemaName = sqlContext.getSchemaName();
    this.sqlExecutor = sqlContext.getJdbcOperations();
    this.mappedModels = sqlContext.getMappedModels();
    this.sqlDialect = sqlContext.getSqlDialect();
    this.sqlExpressionCalculator = sqlContext.getConditionCalculator();
    this.typeHandlerMap = sqlContext.getTypeHandlerMap();
  }

  @Override
  public int insert(String modelName, Map<String, Object> record, Consumer<Object> id) {
    String sql = getInsertSqlString(modelName, record);
    Entity entity = (Entity) sqlContext.getModel(modelName);
    Optional<IDField> idFieldOptional = entity.findIdField();
    if (idFieldOptional.isPresent()) {
      IDField idField = idFieldOptional.get();
      if (record.containsKey(idField.getName())) {
        id.accept(record.get(idField.getName()));
        return sqlExecutor.update(sql, record);
      }
      if (sqlDialect.useFirstGeneratedId()) {
        return sqlExecutor.updateAndReturnFirstGeneratedKeys(sql, record, id::accept);
      }
      return sqlExecutor.updateAndReturnGeneratedKeys(sql, record,
        new String[]{sqlDialect.getGeneratedKeyName(idField.getName())}, keys -> id.accept(keys.getFirst()));
    } else {
      return sqlExecutor.update(sql, record);
    }
  }

  private String getInsertSqlString(String modelName, Map<String, Object> record) {
    String physicalTableName = toPhysicalTablenameQuoteString(modelName);
    StringJoiner columns = new StringJoiner(", ", "(", ")");
    StringJoiner values = new StringJoiner(", ", "(", ")");
    record.forEach((key, value) -> {
      columns.add(sqlDialect.quoteIdentifier(key));
      values.add(":" + key);
    });
    return "insert into " +
           physicalTableName +
           columns +
           " values " +
           values;
  }

  @Override
  public int updateById(String modelName, Map<String, Object> record, Object id) {
    String physicalTableName = toPhysicalTablenameQuoteString(modelName);
    Entity entity = (Entity) sqlContext.getModel(modelName);
    TypedField<?, ?> idField = entity.findIdField().orElseThrow();

    StringBuilder sql = new StringBuilder("update ")
      .append(physicalTableName)
      .append(" set ");
    StringJoiner assignment = new StringJoiner(", ");

    record.keySet().stream().filter(col -> !col.equals(idField.getName()))
      .forEach(col -> assignment.add(sqlDialect.quoteIdentifier(col) + "=:" + col));

    sql.append(assignment);

    sql.append(" where (")
      .append(sqlDialect.quoteIdentifier(idField.getName()))
      .append("=:")
      .append(idField.getName())
      .append(")");

    Map<String, Object> params = new HashMap<>(record);
    params.put(idField.getName(), id);
    return sqlExecutor.update(sql.toString(), params);
  }

  @Override
  public int update(String modelName, Map<String, Object> record, String filter) {
    String physicalTableName = toPhysicalTablenameQuoteString(modelName);
    Entity entity = (Entity) sqlContext.getModel(modelName);
    TypedField<?, ?> idField = entity.findIdField().orElseThrow();
    SqlClauseResult sqlResult = getSqlCauseResult(filter);

    StringBuilder sql = new StringBuilder("update ")
      .append(physicalTableName)
      .append(" set ");
    StringJoiner assignment = new StringJoiner(", ");
    record.keySet().stream()
      .filter(col -> !col.equals(idField.getName()))
      .forEach(col -> assignment.add(sqlDialect.quoteIdentifier(col) + "=:" + col));
    sql.append(assignment);

    sql.append(" where ")
      .append(" (").append(sqlResult.sqlClause()).append(")");

    Map<String, Object> params = new HashMap<>(record);
    params.putAll(sqlResult.args());
    return sqlExecutor.update(sql.toString(), params);
  }

  @Override
  public <T> T findById(String modelName, Object id, Class<T> resultType, boolean nestedQuery) {
    String physicalTableName = toPhysicalTablenameQuoteString(modelName);
    Entity entity = (Entity) sqlContext.getModel(modelName);
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
    Map<String, Object> dataMap = sqlExecutor.queryForObject(sql, Map.of("id", id), getSqlResultHandler(entity, null, Map.class));
    if (nestedQuery && dataMap != null) {
      QueryHelper.nestedQuery(List.of(dataMap), this::findMapList, sqlContext.getModel(modelName), null, sqlContext, sqlContext.getNestedQueryMaxDepth());
    }
    return sqlContext.getJsonObjectConverter().convertValue(dataMap, resultType);
  }

  @Override
  @SuppressWarnings("all")
  public <T> List<T> find(String modelName, Query query, Class<T> resultType) {
    Pair<String, Map<String, Object>> pair = toQuerySqlWithPrepared(modelName, query);
    List mapList = sqlExecutor.queryForList(pair.first(), pair.second(), getSqlResultHandler(sqlContext.getModel(modelName), query, Map.class));
    if (query.isNestedQueryEnabled()) {
      QueryHelper.nestedQuery(mapList, this::findMapList, sqlContext.getModel(modelName), query, sqlContext, sqlContext.getNestedQueryMaxDepth());
    }
    return sqlContext.getJsonObjectConverter().convertValueList(mapList, resultType);
  }

  @Override
  public <T> List<T> findByNativeQuery(String statement, Map<String, Object> params, Class<T> resultType) {
    return sqlExecutor.queryForList(StringHelper.replacePlaceholder(statement), params, new SqlResultHandler<>(resultType));
  }

  @Override
  public <T> List<T> findByNativeQueryModel(String modelName, Map<String, Object> params, Class<T> resultType) {
    NativeQueryModel model = (NativeQueryModel) sqlContext.getModel(modelName);
    String statement = model.getStatement();
    return findByNativeQuery(statement, params, resultType);
  }

  @SuppressWarnings("all")
  private List<Map<String, Object>> findMapList(String modelName, Query query) {
    Pair<String, Map<String, Object>> pair = toQuerySqlWithPrepared(modelName, query);
    List list = sqlExecutor.queryForList(pair.first(), pair.second(), getSqlResultHandler(sqlContext.getModel(modelName), query, Map.class));
    return list;
  }

  @Override
  public long count(String modelName, Query query) {
    Pair<String, Map<String, Object>> pair = toQuerySqlWithPrepared(modelName, query);
    String sql = "select count(*) from(" + pair.first() + ") tmp_count";
    return sqlExecutor.queryForScalar(sql, pair.second(), Long.class);
  }

  @Override
  public int deleteById(String modelName, Object id) {
    String physicalTableName = toPhysicalTablenameQuoteString(modelName);
    Entity entity = (Entity) sqlContext.getModel(modelName);
    TypedField<?, ?> idField = entity.findIdField().orElseThrow();
    String sql = "delete from " +
                 physicalTableName +
                 " where (" + sqlDialect.quoteIdentifier(idField.getName()) + "=:" + idField.getName() + ")";
    return sqlExecutor.update(sql, Map.of(idField.getName(), id));
  }

  @Override
  public int delete(String modelName, String filter) {
    SqlClauseResult sqlResult = getSqlCauseResult(filter);
    String physicalTableName = toPhysicalTablenameQuoteString(modelName);
    String sql = "delete from " +
                 physicalTableName +
                 " where (" +
                 sqlResult.sqlClause() + ")";
    return sqlExecutor.update(sql, sqlResult.args());
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
  private <T> SqlResultHandler<T> getSqlResultHandler(Model model, Query query, Class<T> resultType) {
    SqlResultHandler<T> sqlResultHandler = new SqlResultHandler<>(resultType);
    if (query == null || query.getProjection() == null) {
      for (Field field : model.getFields()) {
        if (field instanceof IDField idField) {
          sqlResultHandler.addSqlTypeHandler(field.getName(), typeHandlerMap.get(idField.getGeneratedValue().getType()));
        } else if (field instanceof TypedField<?, ?> typedField) {
          sqlResultHandler.addSqlTypeHandler(field.getName(), typeHandlerMap.get(typedField.getType()));
        } else if (field instanceof Query.QueryField) {
          sqlResultHandler.addSqlTypeHandler(field.getName(), new UnknownSqlTypeHandler());
        }
      }
    } else {
      Map<String, Query.QueryCall> fields = query.getProjection().getFields();
      for (Map.Entry<String, Query.QueryCall> entry : fields.entrySet()) {
        String key = entry.getKey();
        Query.QueryCall value = entry.getValue();
        sqlResultHandler.addSqlTypeHandler(key, new UnknownSqlTypeHandler());
        if (value instanceof Query.QueryField queryField) {
          if (queryField.getAliasName() != null) {
            Model queryModel = sqlContext.getModel(queryField.getAliasName());
            Field field = queryModel != null
              ? queryModel.getField(queryField.getFieldName())
              : model.getField(queryField.getFieldName());
            if (field instanceof TypedField<?, ?> typedField) {
              sqlResultHandler.addSqlTypeHandler(key, sqlContext.getTypeHandler(typedField.getType()));
            } else {
              sqlResultHandler.addSqlTypeHandler(key, new UnknownSqlTypeHandler());
            }
          } else {
            sqlResultHandler.addSqlTypeHandler(key, new UnknownSqlTypeHandler());
          }
          Field field = queryField.getAliasName() != null
            ? sqlContext.getModel(queryField.getAliasName()).getField(queryField.getFieldName())
            : model.getField(queryField.getFieldName());
          if (field instanceof TypedField<?, ?> typedField) {
            sqlResultHandler.addSqlTypeHandler(key, sqlContext.getTypeHandler(typedField.getType()));
          } else {
            sqlResultHandler.addSqlTypeHandler(key, new UnknownSqlTypeHandler());
          }
        } else {
          sqlResultHandler.addSqlTypeHandler(key, new UnknownSqlTypeHandler());
        }
      }
    }
    return sqlResultHandler;
  }

  private String toPhysicalTablenameQuoteString(String name) {
    Entity entity = (Entity) sqlContext.getModel(name);
    if (entity == null) {
      return sqlDialect.quoteIdentifier(name);
    }
    return sqlDialect.quoteIdentifier(entity.getName());
  }

}
