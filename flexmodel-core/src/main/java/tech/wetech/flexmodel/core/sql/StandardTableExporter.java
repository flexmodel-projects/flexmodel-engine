package tech.wetech.flexmodel.core.sql;

import tech.wetech.flexmodel.core.sql.dialect.SqlDialect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author cjbi@outlook.com
 */
public class StandardTableExporter implements Exporter<SqlTable> {

  protected final SqlDialect sqlDialect;

  public StandardTableExporter(SqlDialect sqlDialect) {
    this.sqlDialect = sqlDialect;
  }

  @Override
  public String[] getSqlCreateString(SqlTable sqlTable) {
    StringBuilder buf = new StringBuilder(sqlDialect.getCreateTableString())
      .append(' ');
    buf.append(sqlDialect.quoteIdentifier(sqlTable.getName()))
      .append(" (");
    Iterator<SqlColumn> iter = sqlTable.getColumnIterator();
    while (iter.hasNext()) {
      SqlColumn col = iter.next();
      String colName = col.getQuotedName(sqlDialect);
      String typeName = sqlDialect.getTypeName(col.getSqlTypeCode(), col.getLength(), col.getPrecision(), col.getScale());
      buf.append(colName)
        .append(' ');
      if (sqlDialect.supportsAutoIncrementColumn() && col.isPrimaryKey() && col.isAutoIncrement()) {
        // to support dialects that have their own identity data type
        if (sqlDialect.hasDataTypeInIdentityColumn()) {
          buf.append(typeName);
        }
        buf.append(' ')
          .append(sqlDialect.getIdentityColumnString(col.getSqlTypeCode()));
      } else {
        buf.append(typeName);
        String defaultValue = sqlDialect.getDefaultValueString(col.getSqlTypeCode(), col.getDefaultValue());
        if (sqlDialect.supportsNotNullWithoutDefaultValue()) {
          if (defaultValue != null) {
            buf.append(" default ").append(defaultValue);
          }
          if (col.isNullable()) {
            buf.append(sqlDialect.getNullColumnString());
          } else {
            buf.append(" not null");
          }
        } else {
          if (defaultValue != null) {
            buf.append(" default ").append(defaultValue);
            if (col.isNullable()) {
              buf.append(sqlDialect.getNullColumnString());
            } else {
              buf.append(" not null");
            }
          }
        }
      }

      if (col.isUnique() && !col.isPrimaryKey()) {
        String keyName = SqlConstraint.generateName("UK_", sqlTable, col);
        SqlUniqueKey uk = sqlTable.getOrCreateUniqueKey(keyName);
        uk.addColumn(col);
      }

      String columnComment = col.getComment();
      if (columnComment != null) {
        buf.append(sqlDialect.getColumnComment(columnComment));
      }
      if (iter.hasNext()) {
        buf.append(", ");
      }
    }

    if (sqlTable.hasPrimaryKey()) {
      buf.append(", ")
        .append(sqlTable.getPrimaryKey().sqlConstraintString(sqlDialect));
    }
    buf.append(')');

    if (sqlTable.getComment() != null) {
      buf.append(sqlDialect.getTableComment(sqlTable.getComment()));
    }

    applyTableTypeString(buf);

    List<String> sqlStrings = new ArrayList<>();
    sqlStrings.add(buf.toString());

    applyComments(sqlTable, sqlStrings);

    return sqlStrings.toArray(new String[0]);
  }

  protected void applyComments(SqlTable sqlTable, List<String> sqlStrings) {
    if (sqlDialect.supportsCommentOn()) {
      if (sqlTable.getComment() != null) {
        sqlStrings.add("comment on table " + sqlDialect.quoteIdentifier(sqlTable.getName()) + " is '" + sqlTable.getComment() + "'");
      }
      final Iterator<SqlColumn> iter = sqlTable.getColumnIterator();
      while (iter.hasNext()) {
        SqlColumn sqlColumn = iter.next();
        String columnComment = sqlColumn.getComment();
        if (columnComment != null) {
          sqlStrings.add("comment on column " + sqlDialect.quoteIdentifier(sqlTable.getName()) + '.' + sqlDialect.quoteIdentifier(sqlColumn.getName()) + " is '" + columnComment + "'");
        }
      }
    }
  }

  protected void applyTableTypeString(StringBuilder buf) {
    buf.append(sqlDialect.getTableTypeString());
  }

  @Override
  public String[] getSqlDropString(SqlTable sqlTable) {
    final StringBuilder buf = new StringBuilder("drop table ");
    if (sqlDialect.supportsIfExistsBeforeTableName()) {
      buf.append("if exists ");
    }
    buf.append(sqlDialect.quoteIdentifier(sqlTable.getName())).append(sqlDialect.getCascadeConstraintsString());
    if (sqlDialect.supportsIfExistsAfterTableName()) {
      buf.append(" if exists");
    }
    return new String[]{buf.toString()};
  }
}
