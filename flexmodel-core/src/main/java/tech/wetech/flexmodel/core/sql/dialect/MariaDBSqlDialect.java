package tech.wetech.flexmodel.core.sql.dialect;

import tech.wetech.flexmodel.core.sql.SqlExecutor;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author cjbi
 */
public class MariaDBSqlDialect extends SqlDialect {

  public MariaDBSqlDialect() {
    registerColumnType(Types.BIT, "bit");
    registerColumnType(Types.BIGINT, "bigint");
    registerColumnType(Types.SMALLINT, "smallint");
    registerColumnType(Types.TINYINT, "tinyint");
    registerColumnType(Types.INTEGER, "integer");
    registerColumnType(Types.CHAR, "char(1)");
    registerColumnType(Types.FLOAT, "float");
    registerColumnType(Types.DOUBLE, "double precision");
    registerColumnType(Types.BOOLEAN, "bit"); // HHH-6935
    registerColumnType(Types.DATE, "date");
    registerColumnType(Types.TIME, "time");
    registerColumnType(Types.TIMESTAMP, "datetime");
    registerColumnType(Types.VARBINARY, "longblob");
    registerColumnType(Types.VARBINARY, 16777215, "mediumblob");
    registerColumnType(Types.VARBINARY, 65535, "blob");
    registerColumnType(Types.VARBINARY, 255, "tinyblob");
    registerColumnType(Types.BINARY, "binary($l)");
    registerColumnType(Types.LONGVARBINARY, "longblob");
    registerColumnType(Types.LONGVARBINARY, 16777215, "mediumblob");
    registerColumnType(Types.NUMERIC, "decimal($p,$s)");
    registerColumnType(Types.BLOB, "longblob");
//		registerColumnType( Types.BLOB, 16777215, "mediumblob" );
//		registerColumnType( Types.BLOB, 65535, "blob" );
    registerColumnType(Types.CLOB, "longtext");
    registerColumnType(Types.NCLOB, "longtext");
//		registerColumnType( Types.CLOB, 16777215, "mediumtext" );
//		registerColumnType( Types.CLOB, 65535, "text" );
    registerVarcharTypes();

    // For details about MySQL 5.7 support for fractional seconds
    // precision (fsp): http://dev.mysql.com/doc/refman/5.7/en/fractional-seconds.html
    // Regarding datetime(fsp), "The fsp value, if given, must be
    // in the range 0 to 6. A value of 0 signifies that there is
    // no fractional part. If omitted, the default precision is 0.
    // (This differs from the standard SQL default of 6, for
    // compatibility with previous MySQL versions.)".

    // The following is defined because Hibernate currently expects
    // the SQL 1992 default of 6 (which is inconsistent with the MySQL
    // default).
//        registerColumnType( Types.TIMESTAMP, "datetime(6)" );
    registerColumnType(Types.TIMESTAMP, "datetime");

    // MySQL 5.7 brings JSON native support with a dedicated datatype.
    // For more details about MySql new JSON datatype support, see:
    // https://dev.mysql.com/doc/refman/5.7/en/json.html
    registerColumnType(Types.JAVA_OBJECT, "json");

    registerColumnType(Types.DECIMAL, "decimal");

    registerFunction("date_format", args -> {
      Map<String, String> utcMap = Map.of(
        "YYYY|yyyy", "%Y",
        "MM", "%m",
        "DD|dd", "%d",
        "hh|HH", "%H",
        "mm", "%i",
        "ss|SS", "%s"
      );
      String fmt = args[1];
      for (Map.Entry<String, String> entry : utcMap.entrySet()) {
        fmt = fmt.replaceAll(Objects.toString(entry.getKey()), entry.getValue());
      }
      return "date_format(" + String.join(",", args[0], fmt) + ")";
    }, 2);
    registerFunction("dayofyear", args -> "dayofyear(" + args[0] + ")", 1);
    registerFunction("dayofmonth", args -> "dayofmonth(" + args[0] + ")", 1);
    registerFunction("dayofweek", args -> "dayofweek(" + args[0] + ")", 1);
  }

  protected void registerVarcharTypes() {
    registerColumnType(Types.VARCHAR, "longtext");
//		registerColumnType( Types.VARCHAR, 16777215, "mediumtext" );
    registerColumnType(Types.VARCHAR, 65535, "varchar($l)");
    registerColumnType(Types.LONGVARCHAR, "longtext");
  }

  @Override
  public String getColumnComment(String comment) {
    return " comment '" + comment + "'";
  }

  @Override
  public boolean supportsIfExistsBeforeTableName() {
    return true;
  }

  @Override
  public String getIdentityColumnString(int type) {
    //starts with 1, implicitly
    return "not null auto_increment";
  }

  @Override
  public String getTableTypeString() {
    return " engine=InnoDB";
  }

  @Override
  public String getTableComment(String comment) {
    return " comment='" + comment + "'";
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
  public String getAddForeignKeyConstraintString(String constraintName, String[] foreignKey, String referencedTable, String[] primaryKey, boolean referencesPrimaryKey) {
    final String cols = String.join(", ", foreignKey);
    final String referencedCols = String.join(", ", primaryKey);
    return String.format(" add constraint %s foreign key (%s) references %s (%s)", constraintName, cols, referencedTable, referencedCols);
  }

  @Override
  public String getDropForeignKeyString() {
    return " drop foreign name ";
  }

  @Override
  public long getSequenceNextVal(String sequenceName, SqlExecutor sqlExecutor) {
    return sqlExecutor.queryForScalar("select next value for " + quoteIdentifier(sequenceName), Long.class);
  }

  @Override
  public String doExtractConstraintName(SQLException ex) {
    String errorMessage = ex.getMessage();
    Pattern compile = Pattern.compile("Duplicate entry '(\\S+)' for key '(\\S+)'");
    Matcher matcher = compile.matcher(errorMessage);
    if (matcher.matches()) {
      String keyName = matcher.group(2);
      String[] strings = keyName.split("\\.");
      return strings[strings.length - 1];
    }
    return null;
  }

}
