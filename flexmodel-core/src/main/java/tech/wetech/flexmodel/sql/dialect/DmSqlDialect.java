package tech.wetech.flexmodel.sql.dialect;

import java.sql.Types;
import java.util.Map;
import java.util.Objects;

/**
 * @author cjbi
 */
public class DmSqlDialect extends SqlDialect {

  public DmSqlDialect() {
    super();

    registerCharacterTypeMappings();
    registerNumericTypeMappings();
    registerDateTimeTypeMappings();
    registerLargeObjectTypeMappings();

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
      return "to_char(" + String.join(", ", args[0], fmt) + ")";
    }, 2);
    registerFunction("dayofyear", args -> "to_number(to_char(" + args[0] + ", 'ddd'))", 1);
    registerFunction("dayofmonth", args -> "to_number(to_char(" + args[0] + ", 'dd'))", 1);
    registerFunction("dayofweek", args -> "to_number(to_char(" + args[0] + ", 'd'))", 1);
  }

  protected void registerCharacterTypeMappings() {
//        registerColumnType(Types.CHAR, "char(1)");
    registerColumnType(Types.CHAR, "varchar2(255)");
    registerColumnType(Types.VARCHAR, 2000, "varchar2($l char)");
    registerColumnType(Types.VARCHAR, "nclob");
  }

  protected void registerNumericTypeMappings() {
    registerColumnType(Types.BIT, "varbinary");
    registerColumnType(Types.BIGINT, "bigint");
    registerColumnType(Types.SMALLINT, "smallint");
    registerColumnType(Types.TINYINT, "tinyint");
    registerColumnType(Types.INTEGER, "int");

    registerColumnType(Types.FLOAT, "float");
    registerColumnType(Types.DOUBLE, "double precision");
    registerColumnType(Types.NUMERIC, "number($p,$s)");
    registerColumnType(Types.DECIMAL, "number($p,$s)");

    registerColumnType(Types.BOOLEAN, "bit");

    registerColumnType(Types.JAVA_OBJECT, "clob");
  }

  protected void registerDateTimeTypeMappings() {
    registerColumnType(Types.DATE, "date");
    registerColumnType(Types.TIME, "time");
//    registerColumnType(Types.TIMESTAMP, "timestamp($l)");
    registerColumnType(Types.TIMESTAMP, "timestamp");
  }

  protected void registerLargeObjectTypeMappings() {
    registerColumnType(Types.BINARY, 2000, "raw($l)");
    registerColumnType(Types.BINARY, "long raw");

    registerColumnType(Types.VARBINARY, 2000, "raw($l)");
    registerColumnType(Types.VARBINARY, "long raw");

    registerColumnType(Types.BLOB, "blob");
    registerColumnType(Types.LONGVARBINARY, "blob");
    registerColumnType(Types.CLOB, "clob");

    registerColumnType(Types.LONGVARCHAR, "clob");
  }

  @Override
  public boolean supportsCommentOn() {
    return true;
  }

  @Override
  public String getIdentityColumnString(int type) throws DialectException {
    return "identity(1,1) not null";
  }

  @Override
  public String getAddColumnString() {
    return "add";
  }

  @Override
  public boolean useFirstGeneratedId() {
    return true;
  }

  @Override
  public boolean supportsGroupByColumnAlias() {
    return false;
  }
}
