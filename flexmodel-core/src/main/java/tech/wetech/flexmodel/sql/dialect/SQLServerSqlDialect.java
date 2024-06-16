package tech.wetech.flexmodel.sql.dialect;

import tech.wetech.flexmodel.sql.SqlExecutor;
import tech.wetech.flexmodel.sql.StandardColumnExporter;

import java.sql.Types;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;

/**
 * 支持 sqlserver 2016以后版本
 *
 * @author cjbi
 */
public class SQLServerSqlDialect extends SqlDialect {

  private static final int MAX_LENGTH = 8000;

  private static final int NVARCHAR_MAX_LENGTH = 4000;

  public SQLServerSqlDialect() {
    super();
    registerColumnType(Types.VARBINARY, "image");
    registerColumnType(Types.VARBINARY, NVARCHAR_MAX_LENGTH, "varbinary($l)");
    registerColumnType(Types.LONGVARBINARY, "image");
    registerColumnType(Types.LONGVARCHAR, "text");
    registerColumnType(Types.BOOLEAN, "bit");

    // HHH-3965 fix
    // As per http://www.sql-server-helper.com/faq/sql-server-2005-varchar-max-p01.aspx
    // use varchar(max) and varbinary(max) instead of TEXT and IMAGE types
    registerColumnType(Types.BLOB, "varbinary(MAX)");
    registerColumnType(Types.VARBINARY, "varbinary(MAX)");
    registerColumnType(Types.VARBINARY, MAX_LENGTH, "varbinary($l)");
    registerColumnType(Types.LONGVARBINARY, "varbinary(MAX)");

    registerColumnType(Types.CLOB, "varchar(MAX)");
    registerColumnType(Types.LONGVARCHAR, "varchar(MAX)");
    registerColumnType(Types.VARCHAR, "varchar(MAX)");
    registerColumnType(Types.VARCHAR, MAX_LENGTH, "varchar($l)");

    registerColumnType(Types.BIGINT, "bigint");
    registerColumnType(Types.BIT, "bit");

    // HHH-8435 fix
    registerColumnType(Types.NCLOB, "nvarchar(MAX)");

    registerColumnType(Types.DATE, "date");
    registerColumnType(Types.TIME, "time");
    registerColumnType(Types.TIMESTAMP, "datetime2");
    registerColumnType(Types.NVARCHAR, NVARCHAR_MAX_LENGTH, "nvarchar($l)");
    registerColumnType(Types.NVARCHAR, "nvarchar(MAX)");

    registerColumnType(Types.JAVA_OBJECT, "varchar(MAX)");

    // supports chinese characters
    registerColumnType(Types.CLOB, "nvarchar(MAX)");
    registerColumnType(Types.LONGVARCHAR, "nvarchar(MAX)");
    registerColumnType(Types.VARCHAR, "nvarchar(MAX)");
    registerColumnType(Types.VARCHAR, MAX_LENGTH, "nvarchar($l)");

    registerFunction("date_format", args -> "format(" + String.join(", ", args[0], args[1]) + ")", 2);
    registerFunction("dayofyear", args -> " datepart(dayofyear, " + args[0] + ")", 1);
    registerFunction("dayofmonth", args -> "day(" + args[0] + ")", 1);
    registerFunction("dayofweek", args -> "datepart(weekday, " + args[0] + ")", 1);
  }

  enum Keyword {

    SELECT("select(\\s+(distinct|all))?"),
    FROM("from"),
    ORDER_BY("order\\s+by"),
    AS("as"),
    WITH("with");

    Pattern pattern;

    Keyword(String keyword) {
      pattern = compile("^\\b" + keyword + "\\b", CASE_INSENSITIVE);
    }

    /**
     * Look for a "root" occurrence of this keyword in
     * the given SQL fragment, that is, an offset where
     * the keyword occurs unquoted and not parenthesized.
     *
     * @param sql a fragment of SQL
     * @return the offset at which the keyword occurs, or
     * 0 if it never occurs outside of quotes or
     * parentheses.
     */
    int rootOffset(String sql) {

      //TODO: does not handle comments

      //use a regex here for its magical ability
      //to match word boundaries and whitespace
      Matcher matcher = pattern.matcher(sql).useTransparentBounds(true);

      int depth = 0;
      boolean quoted = false;
      boolean doubleQuoted = false;
      for (int offset = 0, end = sql.length(); offset < end; ) {
        int nextQuote = sql.indexOf('\'', offset);
        if (nextQuote < 0) {
          nextQuote = end;
        }
        if (!quoted) {
          for (int index = offset; index < nextQuote; index++) {
            switch (sql.charAt(index)) {
              case '(':
                depth++;
                break;
              case ')':
                depth--;
                break;
              case '"':
                doubleQuoted = !doubleQuoted;
                break;
              case '[':
                doubleQuoted = true;
                break;
              case ']':
                doubleQuoted = false;
                break;
              default:
                if (depth == 0 && !doubleQuoted) {
                  matcher.region(index, nextQuote);
                  if (matcher.find()) {
                    //we found the keyword!
                    return index;
                  }
                }
            }
          }
        }
        quoted = !quoted;
        offset = nextQuote + 1;
      }
      return 0; //none found
    }

    int endOffset(String sql, int startOffset) {
      Matcher matcher = pattern.matcher(sql).useTransparentBounds(true);
      matcher.region(startOffset, sql.length());
      matcher.find();
      return matcher.end();
    }

    boolean occursAt(String sql, int offset) {
      Matcher matcher = pattern.matcher(sql).useTransparentBounds(true);
      matcher.region(offset, sql.length());
      return matcher.find();
    }

  }

  protected final SQLServerColumnExporter columnExporter = new SQLServerColumnExporter(this);

  @Override
  public StandardColumnExporter getColumnExporter() {
    return columnExporter;
  }

  @Override
  public String getLimitString(String sql, String offsetPlaceholder, String limitPlaceHolder) {

//    return sql + (" offset " + offsetPlaceholder + " rows fetch next " + limitPlaceHolder + " rows only ");
    //see https://docs.microsoft.com/en-us/sql/t-sql/queries/select-order-by-clause-transact-sql?view=sql-server-2017
    StringBuilder offsetFetch = new StringBuilder();
    offsetFetch.append(sql);
    if (Keyword.ORDER_BY.rootOffset(sql) <= 0) {
      //we need to add a whole 'order by' clause
      offsetFetch.append(" order by ");
      int from = Keyword.FROM.rootOffset(sql);
      if (from > 0) {
        //if we can find the end of the select
        //clause, we will add a dummy column to
        //it below, so order by that column
        // Always need an order by clause: https://blog.jooq.org/2014/05/13/sql-server-trick-circumvent-missing-order-by-clause/
        offsetFetch.append("@@version");
      } else {
        //otherwise order by the first column
        offsetFetch.append("1");
      }
    }
    return offsetFetch.append(" offset " + offsetPlaceholder + " rows fetch next " + limitPlaceHolder + " rows only ").toString();
  }

  @Override
  public boolean supportsIfExistsBeforeTableName() {
    return true;
  }

  @Override
  public String getIdentityColumnString(int type) throws DialectException {
    return "identity not null";
  }

  @Override
  public boolean supportsQualifyIndexName() {
    return false;
  }

  @Override
  public IdentifierCaseStrategy storesIdentifierCaseStrategy() {
    return IdentifierCaseStrategy.LOWER;
  }

  @Override
  public String getAddColumnString() {
    return "add";
  }

  @Override
  public long getSequenceNextVal(String sequenceName, SqlExecutor sqlExecutor) {
    return sqlExecutor.queryForScalar("select next value for " + quoteIdentifier(sequenceName), Long.class);
  }

  @Override
  public boolean supportsGroupByColumnAlias() {
    return false;
  }

    @Override
    public String getModifyColumnString() {
        return "alter column";
    }

}
