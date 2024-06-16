package tech.wetech.flexmodel.sql.dialect;

import tech.wetech.flexmodel.sql.SqlExecutor;
import tech.wetech.flexmodel.sql.StandardColumnExporter;

import java.sql.Types;
import java.util.Map;
import java.util.Objects;

/**
 * @author cjbi
 */
public class DB2SqlDialect extends SqlDialect {

  public DB2SqlDialect() {
    super();
    registerColumnType(Types.BIT, "smallint");
    registerColumnType(Types.BIGINT, "bigint");
    registerColumnType(Types.SMALLINT, "smallint");
    registerColumnType(Types.TINYINT, "smallint");
    registerColumnType(Types.INTEGER, "integer");
    registerColumnType(Types.CHAR, "char(1)");
    registerColumnType(Types.VARCHAR, "varchar($l)");
    registerColumnType(Types.FLOAT, "float");
    registerColumnType(Types.DOUBLE, "double");
    registerColumnType(Types.DATE, "date");
    registerColumnType(Types.TIME, "time");
    registerColumnType(Types.TIMESTAMP, "timestamp");
    registerColumnType(Types.VARBINARY, "varchar($l) for bit data");
    // DB2 converts numeric to decimal under the hood
    // Note that the type returned by DB2 for a numeric column will be Types.DECIMAL. Thus, we have an issue when
    // comparing the types during the schema validation, defining the type to decimal here as the type names will
    // also be compared and there will be a match. See HHH-12827 for the details.
    registerColumnType(Types.NUMERIC, "decimal($p,$s)");
    registerColumnType(Types.DECIMAL, "decimal($p,$s)");
    registerColumnType(Types.BLOB, "blob($l)");
    registerColumnType(Types.CLOB, "clob($l)");
    registerColumnType(Types.LONGVARCHAR, "long varchar");
    registerColumnType(Types.LONGVARBINARY, "long varchar for bit data");
    registerColumnType(Types.BINARY, "varchar($l) for bit data");
    registerColumnType(Types.BINARY, 254, "char($l) for bit data");
    registerColumnType(Types.BOOLEAN, "smallint");
    registerColumnType(Types.JAVA_OBJECT, "varchar(4000)");

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
      return "varchar_format(" + String.join(",", args[0], fmt) + ")";
    }, 2);
    registerFunction("dayofyear", args -> "dayofyear(" + args[0] + ")", 1);
    registerFunction("dayofmonth", args -> "dayofmonth(" + args[0] + ")", 1);
    registerFunction("dayofweek", args -> "dayofweek_iso(" + args[0] + ")", 1);
  }

  protected final DB2ColumnExporter columnExporter = new DB2ColumnExporter(this);

  @Override
  public StandardColumnExporter getColumnExporter() {
    return columnExporter;
  }

  @Override
  public String getIdentityColumnString(int type) throws DialectException {
    return "not null generated always as identity (start with 1 increment by 1)";
  }

  @Override
  public boolean supportsCommentOn() {
    return true;
  }

  @Override
  public long getSequenceNextVal(String sequenceName, SqlExecutor sqlExecutor) {
    return sqlExecutor.queryForScalar("select next value for " + quoteIdentifier(sequenceName) + " from sysibm.sysdummy1", Long.class);
  }

  @Override
  public boolean supportsNotNullWithoutDefaultValue() {
    return false;
  }

  @Override
  public String getModifyColumnString() {
    return "alter column";
  }

  @Override
  public boolean supportsGroupByColumnAlias() {
    return false;
  }

}
