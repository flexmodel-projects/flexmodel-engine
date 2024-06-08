package tech.wetech.flexmodel.sql;

import tech.wetech.flexmodel.sql.dialect.SqlDialect;

import java.util.Iterator;

/**
 * @author cjbi
 */
public class StandardUniqueKeyExporter implements Exporter<SqlUniqueKey> {

  protected final SqlDialect sqlDialect;

  public StandardUniqueKeyExporter(SqlDialect sqlDialect) {
    this.sqlDialect = sqlDialect;
  }

  @Override
  public String[] getSqlCreateString(SqlUniqueKey uniqueKey) {
    String tableName = sqlDialect.quoteIdentifier(uniqueKey.getTable().getName());
    String constraintName = uniqueKey.getName();

    return new String[]{sqlDialect.getAlterTableString(tableName) + " add constraint " + sqlDialect.quoteIdentifier(constraintName) + " " + uniqueConstraintSql(uniqueKey)};
  }

  protected String uniqueConstraintSql(SqlUniqueKey uniqueKey) {
    final StringBuilder sb = new StringBuilder();
    sb.append("unique (");
    final Iterator<SqlColumn> columnIterator = uniqueKey.getColumnIterator();
    while (columnIterator.hasNext()) {
      final SqlColumn column = columnIterator.next();
      sb.append(column.getQuotedName(sqlDialect));
      if (uniqueKey.getColumnOrderMap().containsKey(column)) {
        sb.append(" ").append(uniqueKey.getColumnOrderMap().get(column));
      }
      if (columnIterator.hasNext()) {
        sb.append(", ");
      }
    }

    return sb.append(')').toString();
  }

  @Override
  public String[] getSqlDropString(SqlUniqueKey uniqueKey) {
    String tableName = sqlDialect.quoteIdentifier(uniqueKey.getTable().getName());
    final StringBuilder buf = new StringBuilder(sqlDialect.getAlterTableString(tableName));
    buf.append(getDropUnique());
    if (sqlDialect.supportsIfExistsBeforeConstraintName()) {
      buf.append("if exists ");
    }
    buf.append(sqlDialect.quoteIdentifier(uniqueKey.getName()));
    if (sqlDialect.supportsIfExistsAfterConstraintName()) {
      buf.append(" if exists ");
    }

    return new String[]{buf.toString()};
  }

  protected String getDropUnique() {
    return " drop constraint ";
  }
}
