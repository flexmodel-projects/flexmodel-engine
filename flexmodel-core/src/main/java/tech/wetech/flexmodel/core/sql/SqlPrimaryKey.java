package tech.wetech.flexmodel.core.sql;


import tech.wetech.flexmodel.core.sql.dialect.SqlDialect;

import java.util.Iterator;

/**
 * A primary key constraint
 *
 * @author cjbi
 */
public class SqlPrimaryKey extends SqlConstraint {

  public SqlPrimaryKey(SqlTable sqlTable) {
    setTable(sqlTable);
  }

  @Override
  public void addColumn(SqlColumn sqlColumn) {
    final Iterator<SqlColumn> columnIterator = getTable().getColumnIterator();
    while (columnIterator.hasNext()) {
      final SqlColumn next = columnIterator.next();
      if (next.getCanonicalName().equals(sqlColumn.getCanonicalName())) {
        //Forcing column to be non-null as it is part of the primary key for table.
        next.setNullable(false);
      }
    }
    super.addColumn(sqlColumn);
  }

  public String sqlConstraintString(SqlDialect dialect) {
    StringBuilder buf = new StringBuilder("primary key (");
    Iterator<SqlColumn> iter = getColumnIterator();
    while (iter.hasNext()) {
      buf.append(dialect.quoteIdentifier(iter.next().getName()));
      if (iter.hasNext()) {
        buf.append(", ");
      }
    }
    return buf.append(')').toString();
  }

}
