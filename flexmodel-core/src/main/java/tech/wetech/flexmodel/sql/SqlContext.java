package tech.wetech.flexmodel.sql;

import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.sql.dialect.SqlDialect;
import tech.wetech.flexmodel.sql.type.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cjbi
 */
public class SqlContext extends AbstractSessionContext {
  private final SqlExecutor sqlExecutor;
  private final SqlDialect sqlDialect;

  private SqlExpressionCalculator conditionCalculator;
  protected final Map<String, SqlTypeHandler<?>> typeHandlerMap = new HashMap<>();
  private final SqlMetadata sqlMetadata;

  public SqlContext(String schemaName,
                    SqlExecutor sqlExecutor,
                    MappedModels mappedModels,
                    JsonObjectConverter jsonObjectConverter,
                    SessionFactory factory

  ) {
    super(schemaName, mappedModels, jsonObjectConverter, factory);
    this.sqlExecutor = sqlExecutor;
    DatabaseMetaData databaseMetaData;
    Connection connection = sqlExecutor.getConnection();
    try {
      databaseMetaData = connection.getMetaData();
    } catch (SQLException e) {
      log.error("获取数据库元数据失败了：{}", e.getMessage());
      throw new IllegalStateException("获取数据库连接失败，请检查数据源");
    }
    this.sqlDialect = SqlDialectFactory.create(databaseMetaData);
    this.sqlMetadata = new SqlMetadata(sqlDialect, connection);
    this.conditionCalculator = new DefaultSqlExpressionCalculator(sqlDialect);

    this.typeHandlerMap.put(ScalarType.STRING.getType(), new StringSqlTypeHandler());
    this.typeHandlerMap.put(ScalarType.TEXT.getType(), new TextSqlTypeHandler());
    this.typeHandlerMap.put(ScalarType.DECIMAL.getType(), new DecimalSqlTypeHandler());
    this.typeHandlerMap.put(ScalarType.INT.getType(), new IntSqlTypeHandler());
    this.typeHandlerMap.put(ScalarType.LONG.getType(), new BigintSqlTypeHandler());
    this.typeHandlerMap.put(ScalarType.BOOLEAN.getType(), new BooleanSqlTypeHandler());
    if (sqlDialect.supportsJSR310()) {
      this.typeHandlerMap.put(ScalarType.DATETIME.getType(), new DatetimeSqlTypeHandler());
      this.typeHandlerMap.put(ScalarType.DATE.getType(), new DateSqlTypeHandler());
    } else {
      this.typeHandlerMap.put(ScalarType.DATETIME.getType(), new LegacyDatetimeSqlTypeHandler());
      this.typeHandlerMap.put(ScalarType.DATE.getType(), new LegacyDateSqlTypeHandler());
    }
    this.typeHandlerMap.put(ScalarType.JSON.getType(), new JsonSqlTypeHandler(jsonObjectConverter));
    this.typeHandlerMap.put(ScalarType.ENUM.getType(), new EnumSqlTypeHandler());
  }

  @Override
  public void setPhysicalNamingStrategy(PhysicalNamingStrategy physicalNamingStrategy) {
    this.physicalNamingStrategy = physicalNamingStrategy;
  }

  @Override
  public Map<String, SqlTypeHandler<?>> getTypeHandlerMap() {
    return typeHandlerMap;
  }

  public SqlTypeHandler<?> getTypeHandler(String fieldType) {
    return typeHandlerMap.getOrDefault(fieldType, new UnknownSqlTypeHandler());
  }

  public void addTypeHandler(String fieldType, SqlTypeHandler<?> typeHandler) {
    this.typeHandlerMap.put(fieldType, typeHandler);
  }

  public SqlExecutor getJdbcOperations() {
    return sqlExecutor;
  }

  public SqlDialect getSqlDialect() {
    return sqlDialect;
  }

  public SqlExpressionCalculator getConditionCalculator() {
    return conditionCalculator;
  }

  public void setConditionCalculator(SqlExpressionCalculator conditionCalculator) {
    this.conditionCalculator = conditionCalculator;
  }

  public Connection getConnection() {
    return this.getJdbcOperations().getConnection();
  }

  public SqlMetadata getSqlMetadata() {
    return sqlMetadata;
  }
}
