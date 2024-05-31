package tech.wetech.flexmodel.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.sql.dialect.SqlDialect;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
public class SqlMetadata {

  private final Logger log = LoggerFactory.getLogger(SqlMetadata.class);
  private final String catalog;
  private final String schema;
  private final DatabaseMetaData metaData;
  private final Connection connection;
  private final SqlDialect sqlDialect;

  public SqlMetadata(SqlDialect sqlDialect, Connection connection) {
    try {
      this.sqlDialect = sqlDialect;
      this.connection = connection;
      this.catalog = connection.getCatalog();
      String tmpSchema = null;
      try {
        tmpSchema = connection.getSchema();
      } catch (Exception ignore) {
        // gbase会报错
        // Caused by: java.sql.SQLException: 不支持方法 : IfxSqliConnect.getSchema()
      }
      this.schema = tmpSchema;
      this.metaData = connection.getMetaData();
    } catch (SQLException e) {
      throw new IllegalStateException(e);
    }
  }

  public String getCatalog() {
    return catalog;
  }

  public String getSchema() {
    return schema;
  }

  public List<SqlTable> getTables() {
    return getTables(null);
  }

  /**
   * @param tableNamePattern is nullable
   * @return
   */
  public List<SqlTable> getTables(String tableNamePattern) {
    try (ResultSet tableResultSet = metaData.getTables(catalog, schema, tableNamePattern, new String[]{"TABLE"})) {
      List<SqlTable> tables = new ArrayList<>();
      while (tableResultSet.next()) {
        SqlTable table = new SqlTable();
        table.setName(tableResultSet.getString("TABLE_NAME"));
        log.debug("building table {} struct", table.getName());
        String comment = tableResultSet.getString("REMARKS");
        if (comment != null && !comment.isEmpty()) {
          table.setComment(comment);
        }
        computeColumns(table);
        computePrimaryKey(table);
        computeIndexes(table);
        tables.add(table);
      }
      return tables;
    } catch (SQLException e) {
      log.error("getTables error: {}", e.getMessage(), e);
      throw new IllegalStateException(e);
    } finally {
      try {
        if (this.connection != null) {
          this.connection.close();
        }
      } catch (SQLException e) {
        log.error("close connection error:{}", e.getMessage(), e);
      }
    }
  }

  private void computeIndexes(SqlTable table) throws SQLException {
    traceIfEnabled(table.getName() + " computeIndexes",
      () -> metaData.getIndexInfo(catalog, schema, sqlDialect.quoteIndexInfoTableName(table.getName()), false, false));
    try (ResultSet idxRs = metaData.getIndexInfo(catalog, schema, sqlDialect.quoteIndexInfoTableName(table.getName()), false, false)) {
      String indexName, columnName, ascOrDesc;
      while (idxRs.next()) {
        columnName = idxRs.getString("COLUMN_NAME");
        SqlColumn column = table.getColumn(columnName);
        if (column == null || column.isPrimaryKey()) {
          continue;
        }
        indexName = idxRs.getString("INDEX_NAME");
        SqlIndex index = table.getOrCreateIndex(indexName);
        boolean unique = !idxRs.getBoolean("NON_UNIQUE");
        index.setUnique(unique);
        Map<String, String> ascOrDescMap = new HashMap<>(2);
        ascOrDescMap.put("A", "ASC");
        ascOrDescMap.put("D", "DESC");
        ascOrDesc = ascOrDescMap.get(idxRs.getString("ASC_OR_DESC"));
        index.addColumn(column, ascOrDesc);

      }
    }
  }

  interface ResetSetSupplier {
    ResultSet get() throws SQLException;
  }

  private void traceIfEnabled(String key, ResetSetSupplier consumer) throws SQLException {
    if (log.isTraceEnabled()) {
      log.debug("{} start========================", key);
      ResultSet rs = consumer.get();
      while (rs.next()) {
        log.debug("--------------------------");
        int columnCount = rs.getMetaData().getColumnCount();
        for (int i = 1; i < columnCount; i++) {
          log.debug(rs.getMetaData().getColumnLabel(i) + ":" + rs.getObject(i));
        }
      }
      log.debug("{} end========================", key);
    }
  }

  private void computePrimaryKey(SqlTable table) throws SQLException {
    traceIfEnabled(table.getName() + " computePrimaryKey", () -> metaData.getPrimaryKeys(catalog, schema, table.getName()));
    try (ResultSet pkRs = metaData.getPrimaryKeys(catalog, schema, table.getName())) {
      SqlPrimaryKey primaryKey = null;
      while (pkRs.next()) {
        if (primaryKey == null) {
          primaryKey = new SqlPrimaryKey(table);
          table.setPrimaryKey(primaryKey);
        }

        if (log.isDebugEnabled()) {
          log.debug("sql primary key start----------------------");
          int columnCount = pkRs.getMetaData().getColumnCount();
          for (int i = 1; i < columnCount; i++) {
            log.debug(pkRs.getMetaData().getColumnLabel(i) + ":" + pkRs.getObject(i));
          }
          log.debug("sql primary key end----------------------");
        }

        primaryKey = new SqlPrimaryKey(table);
        table.setPrimaryKey(primaryKey);
        primaryKey.setName(pkRs.getString("PK_NAME"));
        String columnName = pkRs.getString("COLUMN_NAME");
        SqlColumn column = table.getColumn(columnName);
        column.setPrimaryKey(true);
        primaryKey.addColumn(table.getColumn(columnName));
      }

    }
  }

  private void computeColumns(SqlTable table) throws SQLException {
    traceIfEnabled(table.getName() + " computeColumns",
      () -> metaData.getColumns(catalog, schema, table.getName(), null));
    try (ResultSet colRs = metaData.getColumns(catalog, schema, table.getName(), null)) {
      while (colRs.next()) {
        SqlColumn column = new SqlColumn();
        String tableName = colRs.getString("TABLE_NAME");
        String columnName = colRs.getString("COLUMN_NAME");
        int dataType = colRs.getInt("DATA_TYPE");
        int columnSize = colRs.getInt("COLUMN_SIZE");
        int decimalDigits = colRs.getInt("DECIMAL_DIGITS");
        boolean nullable = colRs.getBoolean("NULLABLE");
        String columnDef = colRs.getString("COLUMN_DEF");
        String isAutoincrement = colRs.getString("IS_AUTOINCREMENT");
        column.setTableName(tableName);
        column.setName(columnName);
        column.setSqlTypeCode(dataType);
        column.setLength(columnSize);
        switch (JDBCType.valueOf(dataType)) {
          case BIT, TINYINT, SMALLINT, INTEGER,
            BIGINT, FLOAT, REAL, DOUBLE, NUMERIC, DECIMAL -> {
            column.setPrecision(columnSize);
            column.setScale(decimalDigits);
          }
          default -> column.setLength(columnSize);
        }
        column.setNullable(nullable);
        column.setAutoIncrement("YES".equals(isAutoincrement));
        column.setDefaultValue(columnDef);
        table.addColumn(column);
      }
    }
  }

}
