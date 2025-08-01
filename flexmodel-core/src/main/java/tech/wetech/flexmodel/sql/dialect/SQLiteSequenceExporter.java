package tech.wetech.flexmodel.sql.dialect;

import tech.wetech.flexmodel.sql.SequenceExporter;
import tech.wetech.flexmodel.sql.SqlSequence;

/**
 * @author cjbi
 */
public class SQLiteSequenceExporter extends SequenceExporter {

  public SQLiteSequenceExporter(SqlDialect sqlDialect) {
    super(sqlDialect);
  }

  @Override
  public String[] getSqlCreateString(SqlSequence sequence) {
    String createTableString = String.format("""
      create table %s (
        id integer primary key autoincrement,
        name varchar(100)
      )
      """, sequence.getSequenceName());
    String insertString = String.format("insert into %s (id, name) values (%s, 'sequence')",
      sequence.getSequenceName(), sequence.getInitialValue() - 1 + "");
    return new String[]{createTableString, insertString};
  }

  @Override
  public String[] getSqlDropString(SqlSequence sequence) {
    return new String[]{"drop table if exists " + sequence.getSequenceName()};
  }
}
