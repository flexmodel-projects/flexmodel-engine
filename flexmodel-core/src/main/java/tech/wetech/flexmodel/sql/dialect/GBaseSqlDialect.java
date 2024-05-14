package tech.wetech.flexmodel.sql.dialect;

import tech.wetech.flexmodel.sql.SqlExecutor;

import java.sql.Types;
import java.util.Map;
import java.util.Objects;


/**
 * @author cjbi
 */
public class GBaseSqlDialect extends InformixSqlDialect {

  public GBaseSqlDialect() {
    super();

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
    registerFunction("dayofyear", args -> "to_number(to_char(" + args[0] + ",'DDD'))", 1);
    registerFunction("dayofmonth", args -> "day(" + args[0] + ")", 1);
    registerFunction("dayofweek", args -> "weekday(" + args[0] + ")+1", 1);
  }

  @Override
  public String getIdentityColumnString(int type) throws DialectException {
    return (type == Types.BIGINT ? "bigserial" : "serial") + " not null";
  }

  @Override
  public long getSequenceNextVal(String sequenceName, SqlExecutor sqlExecutor) {
    return sqlExecutor.queryForScalar("select " + quoteIdentifier(sequenceName) + ".nextval from dual", Long.class);
  }

  @Override
  public String getGeneratedKeyName(String name) {
    return name;
  }

  @Override
  public boolean supportsJSR310() {
    return false;
  }
}
