package tech.wetech.flexmodel.sql;

import java.util.List;

/**
 * @author cjbi
 */
public class SqlView implements Exportable {
  private String name;
  private List<String> columnList;
  private String sqlQuery;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getColumnList() {
    return columnList;
  }

  public void setColumnList(List<String> columnList) {
    this.columnList = columnList;
  }

  public String getSqlQuery() {
    return sqlQuery;
  }

  public void setQuery(String query) {
    this.sqlQuery = query;
  }
}
