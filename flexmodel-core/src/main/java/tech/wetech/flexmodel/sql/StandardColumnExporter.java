package tech.wetech.flexmodel.sql;

import tech.wetech.flexmodel.sql.dialect.SqlDialect;

/**
 * @author cjbi
 */
public class StandardColumnExporter implements Exporter<SqlColumn> {

  protected final SqlDialect sqlDialect;

  public StandardColumnExporter(SqlDialect sqlDialect) {
    this.sqlDialect = sqlDialect;
  }

  @Override
  public String[] getSqlCreateString(SqlColumn sqlColumn) {
    StringBuilder alter = new StringBuilder()
      .append(sqlDialect.getAlterTableString(sqlDialect.quoteIdentifier(sqlColumn.getTableName())))
      .append(' ')
      .append(sqlDialect.getAddColumnString())
      .append(' ')
      .append(sqlDialect.quoteIdentifier(sqlColumn.getName()))
      .append(' ')
      .append(sqlDialect.getTypeName(sqlColumn.getSqlTypeCode(), sqlColumn.getLength(), sqlColumn.getPrecision(), sqlColumn.getScale()));

    String defaultValue = sqlDialect.getDefaultValueString(sqlColumn.getSqlTypeCode(), sqlColumn.getDefaultValue());

    if (sqlDialect.supportsNotNullWithoutDefaultValue()) {
      if (defaultValue != null) {
        alter.append(" default ").append(defaultValue);
      }
      if (sqlColumn.isNullable()) {
        alter.append(sqlDialect.getNullColumnString());
      } else {
        alter.append(" not null");
      }
    } else {
      if (defaultValue != null) {
        alter.append(" default ").append(defaultValue);
        if (sqlColumn.isNullable()) {
          alter.append(sqlDialect.getNullColumnString());
        } else {
          alter.append(" not null");
        }
      }
    }
//        if ( column.isUnique() ) {
    //TODO 暂不支持 unique
//        }

//        if ( column.hasCheckConstraint() && dialect.supportsColumnCheck() ) {
    //TODO 暂不支持 表约束
//        }

    String columnComment = sqlColumn.getComment();
    if (columnComment != null) {
      alter.append(sqlDialect.getColumnComment(columnComment));
    }

    alter.append(sqlDialect.getAddColumnSuffixString());
    return new String[]{alter.toString()};
  }

  @Override
  public String[] getSqlDropString(SqlColumn sqlColumn) {
    StringBuilder alter = new StringBuilder()
      .append(sqlDialect.getAlterTableString(sqlDialect.quoteIdentifier(sqlColumn.getTableName())))
      .append(' ')
      .append(sqlDialect.getDropColumnString())
      .append(' ')
      .append(sqlColumn.getQuotedName(sqlDialect));
    return new String[]{alter.toString()};
  }
}
