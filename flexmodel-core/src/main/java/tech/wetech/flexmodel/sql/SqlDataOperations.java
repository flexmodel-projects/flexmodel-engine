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
public class SqlDataOperations implements DataOperations {

  private final SqlContext sqlContext;
  private final String schemaName;
  private final SqlExecutor sqlExecutor;
  private final MappedModels mappedModels;
  private final SqlDialect sqlDialect;
  private final PhysicalNamingStrategy physicalNamingStrategy;
  private final ExpressionCalculator<SqlClauseResult> sqlExpressionCalculator;
  private final Map<String, SqlTypeHandler<?>> typeHandlerMap;

  public SqlDataOperations(SqlContext sqlContext) {
    this.sqlContext = sqlContext;
    this.schemaName = sqlContext.getSchemaName();
    this.sqlExecutor = sqlContext.getJdbcOperations();
    this.mappedModels = sqlContext.getMappedModels();
    this.sqlDialect = sqlContext.getSqlDialect();
    this.physicalNamingStrategy = sqlContext.getPhysicalNamingStrategy();
    this.sqlExpressionCalculator = sqlContext.getConditionCalculator();
    this.typeHandlerMap = sqlContext.getTypeHandlerMap();
  }

  @Override
  public int insert(String modelName, Map<String, Object> record) {
    String sql = getInsertSqlString(modelName, record);
    return sqlExecutor.update(sql, record);
  }

  @Override
  public int insert(String modelName, Map<String, Object> record, Consumer<Object> id) {

    String sql = getInsertSqlString(modelName, record);
    Entity entity = mappedModels.getEntity(schemaName, modelName);
    TypedField<?, ?> idField = entity.idField();
    if (sqlDialect.useFirstGeneratedId()) {
      return sqlExecutor.updateAndReturnFirstGeneratedKeys(sql, record, id::accept);
    }
    return sqlExecutor.updateAndReturnGeneratedKeys(sql, record,
      new String[]{sqlDialect.getGeneratedKeyName(idField.name())}, keys -> id.accept(keys.getFirst()));
  }

  private String getInsertSqlString(String modelName, Map<String, Object> record) {
    String physicalTableName = toPhysicalTablenameQuoteString(modelName);
    StringJoiner columns = new StringJoiner(", ", "(", ")");
    StringJoiner values = new StringJoiner(",", "(", ")");
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
  public int insertAll(String modelName, List<Map<String, Object>> records) {
    if (!sqlDialect.supportsStandardInsertManyValues()) {
      int rows = 0;
      for (Map<String, Object> record : records) {
        rows += insert(modelName, record);
      }
      return rows;
    }
    String physicalTableName = toPhysicalTablenameQuoteString(modelName);
    Entity entity = mappedModels.getEntity(schemaName, modelName);
    TypedField<?, ?> idField = entity.idField();
    boolean isGeneratedId = idField != null && records.getFirst().get(idField.name()) != null;
    List<String> intoColumns = new ArrayList<>();
    for (TypedField<?, ?> field : entity.fields()) {
      if (Objects.equals(idField, field) && !isGeneratedId) {
        continue;
      }
      intoColumns.add(sqlDialect.quoteIdentifier(field.name()));
    }
    List<String> intoValues = new ArrayList<>();
    for (int i = 0; i < records.size(); i++) {
      StringBuilder values = new StringBuilder("( ");
      for (TypedField<?, ?> field : entity.fields()) {
        if (Objects.equals(idField, field) && !isGeneratedId) {
          continue;
        }
        values.append(':').append(field.name()).append('_').append(i).append(", ");
      }
      values.deleteCharAt(values.length() - 2);
      values.append(") ");
      intoValues.add(values.toString());
    }
    String sql = "insert into \n" +
                 physicalTableName +
                 " (" + String.join(", ", intoColumns) + ") \n" +
                 "values " +
                 String.join(",\n", intoValues);

    Map<String, Object> params = new HashMap<>();
    for (int i = 0; i < records.size(); i++) {
      Map<String, Object> record = records.get(i);
      for (TypedField<?, ?> field : entity.fields()) {
        if (Objects.equals(idField, field) && !isGeneratedId) {
          continue;
        }
        params.put(field.name() + "_" + i, record.get(field.name()));
      }
    }
    return sqlExecutor.update(sql, params);
  }

  @Override
  public int updateById(String modelName, Map<String, Object> record, Object id) {
    String physicalTableName = toPhysicalTablenameQuoteString(modelName);
    Entity entity = mappedModels.getEntity(schemaName, modelName);
    TypedField<?, ?> idField = entity.idField();

    StringBuilder sql = new StringBuilder("update ")
      .append(physicalTableName)
      .append(" set ");
    StringJoiner assignment = new StringJoiner(", ");

    record.keySet().stream().filter(col -> !col.equals(idField.name()))
      .forEach(col -> assignment.add(sqlDialect.quoteIdentifier(col) + "=:" + col));

    sql.append(assignment);

    sql.append(" where (")
      .append(sqlDialect.quoteIdentifier(idField.name()))
      .append("=:")
      .append(idField.name())
      .append(")");

    Map<String, Object> params = new HashMap<>(record);
    params.put(idField.name(), id);
    return sqlExecutor.update(sql.toString(), params);
  }

  @Override
  public int update(String modelName, Map<String, Object> record, String filter) {
    String physicalTableName = toPhysicalTablenameQuoteString(modelName);
    Entity entity = mappedModels.getEntity(schemaName, modelName);
    TypedField<?, ?> idField = entity.idField();
    SqlClauseResult sqlResult = getSqlCauseResult(filter);

    StringBuilder sql = new StringBuilder("update ")
      .append(physicalTableName)
      .append(" set ");
    StringJoiner assignment = new StringJoiner(", ");
    record.keySet().stream()
      .filter(col -> !col.equals(idField.name()))
      .forEach(col -> assignment.add(sqlDialect.quoteIdentifier(col) + "=:" + col));
    sql.append(assignment);

    sql.append(" where ")
      .append(" (").append(sqlResult.sqlClause()).append(")");

    Map<String, Object> params = new HashMap<>(record);
    params.putAll(sqlResult.args());
    return sqlExecutor.update(sql.toString(), params);
  }

  @Override
  public <T> T findById(String modelName, Object id, Class<T> resultType) {
    String physicalTableName = toPhysicalTablenameQuoteString(modelName);
    Entity entity = mappedModels.getEntity(schemaName, modelName);
    TypedField<?, ?> idField = entity.idField();
    String columnsString = entity.fields().stream()
      .map(field -> sqlDialect.quoteIdentifier(field.name()))
      .collect(Collectors.joining(", "));

    String sql = " select " +
                 columnsString +
                 " from " +
                 physicalTableName +
                 " " +
                 " where (" + sqlDialect.quoteIdentifier("id") + "= :id)";

    return sqlExecutor.queryForObject(sql, Map.of(idField.name(), id), getSqlResultHandler(entity, null, resultType));
  }

  @Override
  public <T> List<T> find(String modelName, Query query, Class<T> resultType) {
    Model model = mappedModels.getModel(schemaName, modelName);
    Map.Entry<String, Map<String, Object>> entry = SqlHelper.toQuerySqlWithPrepared(sqlContext, modelName, query);
    return sqlExecutor.queryForList(entry.getKey(), entry.getValue(), getSqlResultHandler(model, query, resultType));
  }

  @Override
  public long count(String modelName, Query query) {
    Map.Entry<String, Map<String, Object>> entry = SqlHelper.toQuerySqlWithPrepared(sqlContext, modelName, query);
    String sql = "select count(*) from(" + entry.getKey() + ") tmp_count";
    return sqlExecutor.queryForScalar(sql, entry.getValue(), Long.class);
  }

  @Override
  public int deleteById(String modelName, Object id) {
    String physicalTableName = toPhysicalTablenameQuoteString(modelName);
    Entity entity = mappedModels.getEntity(schemaName, modelName);
    TypedField<?, ?> idField = entity.idField();
    String sql = "delete from " +
                 physicalTableName +
                 " where (" + sqlDialect.quoteIdentifier(idField.name()) + "=:" + idField.name() + ")";
    return sqlExecutor.update(sql, Map.of(idField.name(), id));
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
    if (query == null || query.projection() == null) {
      for (Field field : model.fields()) {
        if (field instanceof IDField idField) {
          sqlResultHandler.addSqlTypeHandler(field.name(), typeHandlerMap.get(idField.generatedValue().type()));
        } else if (field instanceof TypedField<?, ?> typedField) {
          sqlResultHandler.addSqlTypeHandler(field.name(), typeHandlerMap.get(typedField.type()));
        } else if (field instanceof Query.QueryField) {
          sqlResultHandler.addSqlTypeHandler(field.name(), new UnknownSqlTypeHandler());
        }
      }
    } else {
      query.projection()
        .fields()
        .keySet()
        .forEach(key -> sqlResultHandler.addSqlTypeHandler(key, new UnknownSqlTypeHandler()));
    }

    return sqlResultHandler;
  }

  private String toPhysicalTablenameQuoteString(String name) {
    return sqlDialect.quoteIdentifier(physicalNamingStrategy.toPhysicalTableName(name));
  }

}
