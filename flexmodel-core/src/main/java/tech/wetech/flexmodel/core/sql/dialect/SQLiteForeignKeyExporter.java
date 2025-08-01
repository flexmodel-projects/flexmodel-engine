package tech.wetech.flexmodel.core.sql.dialect;

import tech.wetech.flexmodel.core.sql.SqlForeignKey;
import tech.wetech.flexmodel.core.sql.StandardForeignKeyExporter;

/**
 * @author cjbi
 */
public class SQLiteForeignKeyExporter extends StandardForeignKeyExporter {

  private final SqlDialect dialect;

  public SQLiteForeignKeyExporter(SqlDialect dialect) {
    super(dialect);
    this.dialect = dialect;
  }

  @Override
  public String[] getSqlCreateString(SqlForeignKey exportable) {
    // todo something
    return new String[0];
  }

  @Override
  public String[] getSqlDropString(SqlForeignKey exportable) {
    // todo something
    return new String[0];
  }
}
