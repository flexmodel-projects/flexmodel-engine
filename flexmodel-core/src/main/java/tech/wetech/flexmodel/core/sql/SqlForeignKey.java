package tech.wetech.flexmodel.core.sql;

import java.util.ArrayList;
import java.util.List;

/**
 * A foreign name constraint
 *
 * @author cjbi
 */
public class SqlForeignKey extends SqlConstraint {

  private SqlTable referencedTable;
  private boolean cascadeDeleteEnabled;
  private List<SqlColumn> referencedColumns = new ArrayList<>();

  public SqlForeignKey() {
  }

  public SqlTable getReferencedTable() {
    return referencedTable;
  }

  public void setReferencedTable(SqlTable referencedTable) {
    this.referencedTable = referencedTable;
  }

  public boolean isCascadeDeleteEnabled() {
    return cascadeDeleteEnabled;
  }

  public void setCascadeDeleteEnabled(boolean cascadeDeleteEnabled) {
    this.cascadeDeleteEnabled = cascadeDeleteEnabled;
  }

  public List<SqlColumn> getReferencedColumns() {
    return referencedColumns;
  }

  public void setReferencedColumns(List<SqlColumn> referencedColumns) {
    this.referencedColumns = referencedColumns;
  }

  /**
   * Does this foreignkey reference the primary key of the reference table
   */
  public boolean isReferenceToPrimaryKey() {
    return referencedColumns.isEmpty();
  }

}
