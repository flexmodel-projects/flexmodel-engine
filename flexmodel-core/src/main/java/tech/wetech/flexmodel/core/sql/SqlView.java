package tech.wetech.flexmodel.core.sql;

import java.util.List;

/**
 * @author cjbi
 */
public class SqlView implements Exportable {
  private String name;
  private List<String> columnList;
  private String statement;

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

  public String getStatement() {
    return statement;
  }

  public void setStatement(String statement) {
    this.statement = statement;
  }

}
