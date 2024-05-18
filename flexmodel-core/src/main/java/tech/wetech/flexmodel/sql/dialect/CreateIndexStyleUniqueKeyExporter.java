package tech.wetech.flexmodel.sql.dialect;

import tech.wetech.flexmodel.sql.SqlColumn;
import tech.wetech.flexmodel.sql.SqlUniqueKey;
import tech.wetech.flexmodel.sql.StandardUniqueKeyExporter;

import java.util.StringJoiner;

/**
 * @author cjbi
 */
public class CreateIndexStyleUniqueKeyExporter extends StandardUniqueKeyExporter {

  public CreateIndexStyleUniqueKeyExporter(SqlDialect dialect) {
    super(dialect);
  }

  @Override
  public String[] getSqlCreateString(SqlUniqueKey uniqueKey) {
    StringJoiner columns = new StringJoiner(", ");
    for (SqlColumn sqlColumn : uniqueKey.getColumns()) {
      columns.add(sqlColumn.getQuotedName(sqlDialect));
    }
    return new String[]{"create unique index " + uniqueKey.getName() +
                        " on " + sqlDialect.quoteIdentifier(uniqueKey.getTable().getName()) + " (" + columns + ")"};
  }

  @Override
  public String[] getSqlDropString(SqlUniqueKey uniqueKey) {
    return new String[]{"drop index " + uniqueKey.getName()};
  }
}
