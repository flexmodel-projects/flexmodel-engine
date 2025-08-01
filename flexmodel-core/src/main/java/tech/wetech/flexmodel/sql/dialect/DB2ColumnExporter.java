package tech.wetech.flexmodel.sql.dialect;

import tech.wetech.flexmodel.sql.SqlColumn;
import tech.wetech.flexmodel.sql.StandardColumnExporter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cjbi
 */
public class DB2ColumnExporter extends StandardColumnExporter {

  private final SqlDialect sqlDialect;

  public DB2ColumnExporter(SqlDialect sqlDialect) {
    super(sqlDialect);
    this.sqlDialect = sqlDialect;
  }

  @Override
  public String[] getSqlCreateString(SqlColumn sqlColumn) {
    String[] sqlCreateString = super.getSqlCreateString(sqlColumn);
    return applyReorgTableString(sqlColumn, sqlCreateString);
  }

  @Override
  public String[] getSqlDropString(SqlColumn sqlColumn) {
    String[] sqlDropString = super.getSqlDropString(sqlColumn);
    return applyReorgTableString(sqlColumn, sqlDropString);
  }

  private String[] applyReorgTableString(SqlColumn sqlColumn, String[] sqlDropString) {
    String reorgString = String.format("call sysproc.admin_cmd('reorg table %s')",
      sqlDialect.quoteIdentifier(sqlColumn.getTableName()));
    List<String> sqlStrings = new ArrayList<>(List.of(sqlDropString));
    sqlStrings.add(reorgString);
    return sqlStrings.toArray(new String[]{});
  }
}
