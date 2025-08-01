package tech.wetech.flexmodel.core.sql;

import tech.wetech.flexmodel.core.sql.dialect.SqlDialect;

/**
 * @author cjbi
 */
public abstract class SequenceExporter implements Exporter<SqlSequence> {

  protected final SqlDialect sqlDialect;

  protected SequenceExporter(SqlDialect sqlDialect) {
    this.sqlDialect = sqlDialect;
  }
}
