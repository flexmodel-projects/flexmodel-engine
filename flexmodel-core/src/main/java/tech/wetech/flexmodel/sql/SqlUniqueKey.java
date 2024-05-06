package tech.wetech.flexmodel.sql;

import java.util.HashMap;
import java.util.Map;

/**
 * A relational unique name constraint
 *
 * @author cjbi
 */
public class SqlUniqueKey extends SqlConstraint {
  private Map<SqlColumn, String> columnOrderMap = new HashMap<>();

  public void addColumn(SqlColumn column, String order) {
    addColumn(column);
    if (StringHelper.isNotEmpty(order)) {
      columnOrderMap.put(column, order);
    }
  }

  public Map<SqlColumn, String> getColumnOrderMap() {
    return columnOrderMap;
  }
}
