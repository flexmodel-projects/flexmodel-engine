package tech.wetech.flexmodel.sql.dialect;

import tech.wetech.flexmodel.sql.SqlExecutor;

import java.sql.Types;
import java.util.Map;
import java.util.Objects;

/**
 * @author cjbi
 */
public class PostgreSQLSqlDialect extends SqlDialect {

  public PostgreSQLSqlDialect() {
    super();
    registerColumnType(Types.BIT, "bool");
    registerColumnType(Types.BIGINT, "int8");
    registerColumnType(Types.SMALLINT, "int2");
    registerColumnType(Types.TINYINT, "int2");
    registerColumnType(Types.INTEGER, "int4");
    registerColumnType(Types.CHAR, "char(1)");
    registerColumnType(Types.VARCHAR, "varchar($l)");
    registerColumnType(Types.FLOAT, "float4");
    registerColumnType(Types.DOUBLE, "float8");
    registerColumnType(Types.DATE, "date");
    registerColumnType(Types.TIME, "time");
    registerColumnType(Types.TIMESTAMP, "timestamp");
    registerColumnType(Types.VARBINARY, "bytea");
    registerColumnType(Types.BINARY, "bytea");
    registerColumnType(Types.LONGVARCHAR, "text");
    registerColumnType(Types.LONGVARBINARY, "bytea");
    registerColumnType(Types.CLOB, "oid");
//    registerColumnType(Types.BLOB, "oid");
    registerColumnType(Types.BLOB, "bytea");
    registerColumnType(Types.NUMERIC, "numeric($p, $s)");
    registerColumnType(Types.OTHER, "uuid");

    // ext
    registerColumnType(Types.JAVA_OBJECT, "varchar");

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
    registerFunction("dayofyear", args -> "extract(doy from " + args[0] + ")", 1);
    registerFunction("dayofmonth", args -> "extract(days from " + args[0] + ")", 1);
    registerFunction("dayofweek", args -> "extract(isodow from " + args[0] + ")", 1);
  }

  @Override
  public boolean supportsCommentOn() {
    return true;
  }

  @Override
  public boolean supportsIfExistsBeforeTableName() {
    return true;
  }

  @Override
  public IdentifierCaseStrategy storesIdentifierCaseStrategy() {
    return IdentifierCaseStrategy.LOWER;
  }

  @Override
  public String getIdentityColumnString(int type) throws DialectException {
    switch (type) {
      case Types.BIGINT:
        return "bigserial not null";
      case Types.INTEGER:
        return "serial not null";
      default:
        throw new DialectException("illegal identity column type");
    }
  }

  @Override
  public boolean hasDataTypeInIdentityColumn() {
    return false;
  }

  @Override
  public long getSequenceNextVal(String sequenceName, SqlExecutor sqlExecutor) {
    return sqlExecutor.queryForScalar("select nextval ('" + sequenceName + "')", Long.class);
  }

  @Override
  public String getGeneratedKeyName(String name) {
    return name;
  }

  @Override
  public String toBooleanValueString(boolean bool) {
    return bool + "";
  }

    @Override
    public String getModifyColumnString() {
        return "alter column";
    }

}
