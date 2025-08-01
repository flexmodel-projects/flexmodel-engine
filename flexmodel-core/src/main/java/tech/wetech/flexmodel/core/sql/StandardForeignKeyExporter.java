package tech.wetech.flexmodel.core.sql;

import tech.wetech.flexmodel.core.sql.dialect.SqlDialect;

import java.util.Iterator;

/**
 * @author cjbi
 */
public class StandardForeignKeyExporter implements Exporter<SqlForeignKey> {

  private final SqlDialect dialect;

  public StandardForeignKeyExporter(SqlDialect dialect) {
    this.dialect = dialect;
  }

  @Override
  public String[] getSqlCreateString(SqlForeignKey foreignKey) {
    final int numberOfColumns = foreignKey.getColumnSpan();
    final String[] columnNames = new String[numberOfColumns];
    final String[] targetColumnNames = new String[numberOfColumns];

    final Iterator<SqlColumn> targetItr;
    if (foreignKey.isReferenceToPrimaryKey()) {
      targetItr = foreignKey.getReferencedTable().getPrimaryKey().getColumnIterator();
    } else {
      targetItr = foreignKey.getReferencedColumns().iterator();
    }
    int i = 0;
    final Iterator<SqlColumn> itr = foreignKey.getColumnIterator();
    while (itr.hasNext()) {
      columnNames[i] = (itr.next()).getQuotedName(dialect);
      targetColumnNames[i] = (targetItr.next()).getQuotedName(dialect);
      i++;
    }
    String sourceTableName = dialect.quoteIdentifier(foreignKey.getTable().getName());
    String targetTableName = dialect.quoteIdentifier(foreignKey.getReferencedTable().getName());
    final StringBuilder buffer = new StringBuilder(dialect.getAlterTableString(sourceTableName))
      .append(
        dialect.getAddForeignKeyConstraintString(
          foreignKey.getName(),
          columnNames,
          targetTableName,
          targetColumnNames,
          foreignKey.isReferenceToPrimaryKey()
        )
      );
    if (dialect.supportsCascadeDelete()) {
      if (foreignKey.isCascadeDeleteEnabled()) {
        buffer.append(" on delete cascade");
      }
    }

    return new String[]{
      buffer.toString()
    };
  }

  @Override
  public String[] getSqlDropString(SqlForeignKey foreignKey) {
    String tableName = foreignKey.getTable().getName();
    final StringBuilder buf = new StringBuilder(dialect.getAlterTableString(tableName));
    buf.append(dialect.getDropForeignKeyString());
    if (dialect.supportsIfExistsBeforeConstraintName()) {
      buf.append("if exists ");
    }
    buf.append(foreignKey.getName());
    if (dialect.supportsIfExistsAfterConstraintName()) {
      buf.append(" if exists");
    }
    return new String[]{buf.toString()};
  }
}
