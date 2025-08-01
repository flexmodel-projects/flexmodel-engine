package tech.wetech.flexmodel.sql.dialect;

import java.sql.Types;
import java.util.Map;
import java.util.Objects;

/**
 * 支持 12c 以后的数据库版本
 *
 * @author cjbi
 */
public class OracleSqlDialect extends SqlDialect {

  public OracleSqlDialect() {
    super();
    registerCharacterTypeMappings();
    registerNumericTypeMappings();
    registerDateTimeTypeMappings();
    registerLargeObjectTypeMappings();

    registerColumnType(Types.BIT, "number(1,0)");
    registerColumnType(Types.BIGINT, "number(19,0)");
    registerColumnType(Types.SMALLINT, "number(5,0)");
    registerColumnType(Types.TINYINT, "number(3,0)");
    registerColumnType(Types.INTEGER, "number(10,0)");
    registerColumnType(Types.CHAR, "char(1 char)");
    registerColumnType(Types.VARCHAR, 4000, "varchar2($l char)");
    registerColumnType(Types.VARCHAR, "long");
    registerColumnType(Types.FLOAT, "float");
    registerColumnType(Types.DOUBLE, "double precision");
    registerColumnType(Types.DATE, "date");
    registerColumnType(Types.TIME, "date");
    registerColumnType(Types.TIMESTAMP, "timestamp");
    registerColumnType(Types.VARBINARY, 2000, "raw($l)");
    registerColumnType(Types.VARBINARY, "long raw");
    registerColumnType(Types.NUMERIC, "number($p,$s)");
    registerColumnType(Types.DECIMAL, "number($p,$s)");
    registerColumnType(Types.BLOB, "blob");
    registerColumnType(Types.CLOB, "clob");

    registerColumnType(Types.JAVA_OBJECT, "varchar2(4000 char)");

    registerFunction("date_format", args -> {
      Map<String, String> utcMap = Map.of(
        "YYYY|yyyy", "YYYY",
        "MM", "MM",
        "DD|dd", "DD",
        "hh|HH", "HH24",
        "mm", "MI",
        "ss|SS", "SS"
      );
      String fmt = args[1];
      for (Map.Entry<String, String> entry : utcMap.entrySet()) {
        fmt = fmt.replaceAll(Objects.toString(entry.getKey()), entry.getValue());
      }
      return "to_char(" + String.join(",", args[0], fmt) + ")";
    }, 2);
    registerFunction("dayofyear", args -> "to_number(to_char(" + args[0] + ", 'DDD'))", 1);
    registerFunction("dayofmonth", args -> "extract(day from " + args[0] + ")", 1);
    registerFunction("dayofweek", args -> "to_number(to_char(" + args[0] + ", 'D'))", 1);
  }

  protected void registerCharacterTypeMappings() {
    registerColumnType(Types.CHAR, "char(1 char)");
    registerColumnType(Types.VARCHAR, 4000, "varchar2($l char)");
    registerColumnType(Types.VARCHAR, "long");
    registerColumnType(Types.NVARCHAR, "nvarchar2($l)");
    registerColumnType(Types.LONGNVARCHAR, "nvarchar2($l)");
  }

  protected void registerNumericTypeMappings() {
    registerColumnType(Types.BIT, "number(1,0)");
    registerColumnType(Types.BIGINT, "number(19,0)");
    registerColumnType(Types.SMALLINT, "number(5,0)");
    registerColumnType(Types.TINYINT, "number(3,0)");
    registerColumnType(Types.INTEGER, "number(10,0)");

    registerColumnType(Types.FLOAT, "float");
    registerColumnType(Types.DOUBLE, "double precision");
    registerColumnType(Types.NUMERIC, "number($p,$s)");
    registerColumnType(Types.DECIMAL, "number($p,$s)");

    registerColumnType(Types.BOOLEAN, "number(1,0)");
  }

  protected void registerDateTimeTypeMappings() {
    registerColumnType(Types.DATE, "date");
    registerColumnType(Types.TIME, "date");
    registerColumnType(Types.TIMESTAMP, "timestamp");
  }

  protected void registerLargeObjectTypeMappings() {
    registerColumnType(Types.BINARY, 2000, "raw($l)");
    registerColumnType(Types.BINARY, "long raw");

    registerColumnType(Types.VARBINARY, 2000, "raw($l)");
    registerColumnType(Types.VARBINARY, "long raw");

    registerColumnType(Types.BLOB, "blob");
    registerColumnType(Types.CLOB, "clob");

    registerColumnType(Types.LONGVARCHAR, "long");
    registerColumnType(Types.LONGVARBINARY, "long raw");

    registerColumnType(Types.LONGVARCHAR, "clob");
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
  public String getIdentityColumnString(int type) {
    return "generated as identity";
  }

  @Override
  public String quoteIndexInfoTableName(String identifier) {
    return quoteIdentifier(identifier);
  }

  @Override
  public boolean supportsCommentOn() {
    return true;
  }

  @Override
  public String getLimitString(String sql, String offsetPlaceholder, String limitPlaceHolder) {
    return getOffsetFetchRowsString(sql, offsetPlaceholder, limitPlaceHolder);
  }

  @Override
  public boolean supportsGroupByColumnAlias() {
    return false;
  }

}
