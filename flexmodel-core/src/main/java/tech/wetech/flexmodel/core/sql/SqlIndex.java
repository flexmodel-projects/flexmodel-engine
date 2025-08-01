package tech.wetech.flexmodel.core.sql;

import java.util.*;

/**
 * A relational table index
 *
 * @author cjbi@outlook.com
 */
class SqlIndex implements Exportable {
  private SqlTable sqlTable;
  private final List<SqlColumn> sqlColumns = new ArrayList<>();
  private final Map<SqlColumn, String> columnOrderMap = new HashMap<>();
  private String name;
  private boolean unique;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setTable(SqlTable sqlTable) {
    this.sqlTable = sqlTable;
  }

  public SqlTable getTable() {
    return sqlTable;
  }

  public void addColumn(SqlColumn sqlColumn) {
    if (!sqlColumns.contains(sqlColumn)) {
      sqlColumns.add(sqlColumn);
    }
  }

  public void addColumn(SqlColumn sqlColumn, String order) {
    addColumn(sqlColumn);
    if (StringHelper.isNotEmpty(order)) {
      columnOrderMap.put(sqlColumn, order);
    }
  }

  public List<SqlColumn> getColumns() {
    return sqlColumns;
  }

  public Map<SqlColumn, String> getColumnOrderMap() {
    return columnOrderMap;
  }

  public Iterator<SqlColumn> getColumnIterator() {
    return sqlColumns.iterator();
  }

  public boolean isUnique() {
    return unique;
  }

  public SqlIndex setUnique(boolean unique) {
    this.unique = unique;
    return this;
  }

}
