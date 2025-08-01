package tech.wetech.flexmodel.core.sql.dialect;

import tech.wetech.flexmodel.core.sql.SequenceExporter;
import tech.wetech.flexmodel.core.sql.SqlSequence;

/**
 * @author cjbi
 */
public class MySQLSequenceExporter extends SequenceExporter {

  public MySQLSequenceExporter(SqlDialect sqlDialect) {
    super(sqlDialect);
  }

  @Override
  public String[] getSqlCreateString(SqlSequence sequence) {
    String createTableString = String.format("""
      create table %s (
        nextval int not null,
        incr     int not null
      ) engine=innodb
      """, sequence.getSequenceName());
    String insertString = String.format("insert into %s (nextval, incr) values (%s, %s)",
      sequence.getSequenceName(), sequence.getInitialValue() - 1, sequence.getIncrementSize());
    return new String[]{createTableString, insertString};
  }

  @Override
  public String[] getSqlDropString(SqlSequence sequence) {
    return new String[]{"drop table if exists " + sequence.getSequenceName()};
  }
}
