package tech.wetech.flexmodel.sql;

import tech.wetech.flexmodel.sql.dialect.SqlDialect;

/**
 * @author cjbi
 */
public abstract class SequenceExporter implements Exporter<SqlSequence> {

  protected final SqlDialect sqlDialect;

  protected SequenceExporter(SqlDialect sqlDialect) {
    this.sqlDialect = sqlDialect;
  }
}
