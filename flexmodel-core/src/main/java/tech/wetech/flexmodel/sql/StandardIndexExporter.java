package tech.wetech.flexmodel.sql;

import tech.wetech.flexmodel.sql.dialect.SqlDialect;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;

/**
 * @author cjbi@outlook.com
 */
public class StandardIndexExporter implements Exporter<SqlIndex> {

  protected final SqlDialect dialect;

  public StandardIndexExporter(SqlDialect dialect) {
    this.dialect = dialect;
  }

  @Override
  public String[] getSqlCreateString(SqlIndex index) {
    final String tableName = index.getTable().getName();

    final String indexNameForCreation;
    indexNameForCreation = index.getName();
    final StringBuilder buf = new StringBuilder()
      .append("create ");

    if (index.isUnique()) {
      buf.append("unique ");
    }

    buf.append("index ");

    buf.append(indexNameForCreation)
      .append(" on ")
      .append(dialect.quoteIdentifier(tableName))
      .append(" (");

    boolean first = true;
    final Iterator<SqlColumn> columnItr = index.getColumnIterator();
    final Map<SqlColumn, String> columnOrderMap = index.getColumnOrderMap();
    while (columnItr.hasNext()) {
      final SqlColumn sqlColumn = columnItr.next();
      if (first) {
        first = false;
      } else {
        buf.append(", ");
      }
      buf.append(sqlColumn.getQuotedName(dialect));
      if (columnOrderMap.containsKey(sqlColumn)) {
        buf.append(" ").append(columnOrderMap.get(sqlColumn));
      }
    }
    buf.append(")");
    return new String[]{buf.toString()};
  }


  @Override
  public String[] getSqlDropString(SqlIndex index) {
    final String tableName =  dialect.quoteIdentifier(index.getTable().getName());
    if (!dialect.dropConstraints()) {
      return NO_COMMANDS;
    }

    final String indexNameForCreation = dialect.supportsQualifyIndexName()
//      ? StringHelper.qualify(tableName, index.getName())
      ? index.getName()
      : index.getName() + " on " + tableName;

    return new String[]{"drop index " + indexNameForCreation};
  }

  // length用户要求产生字符串的长度
  public static String getRandomString(int length) {
    String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    Random random = new Random();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < length; i++) {
      int number = random.nextInt(62);
      sb.append(str.charAt(number));
    }
    return sb.toString();
  }

}
