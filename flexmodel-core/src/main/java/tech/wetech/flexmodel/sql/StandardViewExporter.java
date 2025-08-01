package tech.wetech.flexmodel.sql;

import tech.wetech.flexmodel.sql.dialect.SqlDialect;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cjbi
 */
public class StandardViewExporter implements Exporter<SqlView> {

  private final SqlDialect sqlDialect;

  public StandardViewExporter(SqlDialect sqlDialect) {
    this.sqlDialect = sqlDialect;
  }

  @Override
  public String[] getSqlCreateString(SqlView sqlView) {
    StringBuilder buf = new StringBuilder()
      .append("create view")
      .append(' ')
      .append(sqlDialect.quoteIdentifier(sqlView.getName()));
    List<String> columnList = sqlView.getColumnList();
    if (columnList != null && !columnList.isEmpty()) {
      buf.append(" (");
      buf.append(
        columnList.stream()
          .map(sqlDialect::quoteIdentifier)
          .collect(Collectors.joining(", "))
      );
      buf.append(')');
    }
    buf.append(" as ");
    buf.append(sqlView.getStatement());
    return new String[]{buf.toString()};
  }

  @Override
  public String[] getSqlDropString(SqlView sqlView) {
    String buf = "drop view" +
                 ' ' +
                 sqlDialect.quoteIdentifier(sqlView.getName());
    return new String[]{buf};
  }
}
