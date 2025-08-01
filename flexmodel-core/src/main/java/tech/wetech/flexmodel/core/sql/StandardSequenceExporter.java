package tech.wetech.flexmodel.core.sql;

import tech.wetech.flexmodel.core.sql.dialect.SqlDialect;

/**
 * @author cjbi
 */
public class StandardSequenceExporter extends SequenceExporter {

  public StandardSequenceExporter(SqlDialect sqlDialect) {
    super(sqlDialect);
  }

  @Override
  public String[] getSqlCreateString(SqlSequence sequence) {
    return new String[]{
      String.format(
        "create sequence %s start with %s increment by %s",
        sqlDialect.quoteIdentifier(sequence.getSequenceName()),
        sequence.getInitialValue(),
        sequence.getIncrementSize()
      )};
  }

  @Override
  public String[] getSqlDropString(SqlSequence sequence) {
    return new String[]{"drop sequence " + sqlDialect.quoteIdentifier(sequence.getSequenceName())};
  }
}
