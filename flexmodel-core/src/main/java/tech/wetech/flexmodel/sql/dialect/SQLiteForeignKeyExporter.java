package tech.wetech.flexmodel.sql.dialect;

import tech.wetech.flexmodel.sql.SqlForeignKey;
import tech.wetech.flexmodel.sql.StandardForeignKeyExporter;

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
