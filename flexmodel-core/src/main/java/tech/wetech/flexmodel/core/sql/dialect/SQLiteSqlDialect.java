package tech.wetech.flexmodel.core.sql.dialect;

import tech.wetech.flexmodel.core.sql.SequenceExporter;
import tech.wetech.flexmodel.core.sql.SqlExecutor;
import tech.wetech.flexmodel.core.sql.StandardForeignKeyExporter;

import java.sql.Types;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author cjbi
 */
public class SQLiteSqlDialect extends SqlDialect {

  public SQLiteSqlDialect() {
    super();

    // Sqlite doesn't allow BIGINT used as an primary key with autoincrement.
    // AUTOINCREMENT is only allowed on an INTEGER PRIMARY KEY
    registerColumnType(Types.BIGINT, "integer");

    registerColumnType(Types.BIT, "boolean");
    registerColumnType(Types.FLOAT, "float");
    registerColumnType(Types.DOUBLE, "double");
    registerColumnType(Types.DECIMAL, "decimal");
    registerColumnType(Types.CHAR, "char");
    registerColumnType(Types.LONGVARCHAR, "longvarchar");
    registerColumnType(Types.TIMESTAMP, "datetime");
    registerColumnType(Types.BINARY, "blob");
    registerColumnType(Types.VARBINARY, "blob");
    registerColumnType(Types.LONGVARBINARY, "blob");
    registerColumnType(Types.JAVA_OBJECT, "longvarchar");

    registerFunction("date_format", args -> {
      Map<String, String> utcMap = Map.of(
        "YYYY|yyyy", "%Y",
        "MM", "%m",
        "DD|dd", "%d",
        "hh|HH", "%H",
        "mm", "%M",
        "ss|SS", "%S"
      );
      String fmt = args[1];
      for (Map.Entry<String, String> entry : utcMap.entrySet()) {
        fmt = fmt.replaceAll(Objects.toString(entry.getKey()), entry.getValue());
      }
      return "strftime(" + String.join(",", fmt, args[0]) + ")";
    }, 2);
    registerFunction("dayofyear", args -> "cast(strftime('%j', " + args[0] + ") as integer)", 1);
    registerFunction("dayofmonth", args -> "cast(strftime('%d', " + args[0] + ") as integer)", 1);
    registerFunction("dayofweek", args -> "cast(strftime('%w', " + args[0] + ") as integer)", 1);
  }

  protected final SequenceExporter sequenceExporter = new SQLiteSequenceExporter(this);
  protected final SQLiteForeignKeyExporter foreignKeyExporter = new SQLiteForeignKeyExporter(this);
  protected final CreateIndexStyleUniqueKeyExporter uniqueKeyExporter = new CreateIndexStyleUniqueKeyExporter(this);

  @Override
  public SequenceExporter getSequenceExporter() {
    return sequenceExporter;
  }

  @Override
  public StandardForeignKeyExporter getForeignKeyExporter() {
    return foreignKeyExporter;
  }

  @Override
  public CreateIndexStyleUniqueKeyExporter getUniqueKeyExporter() {
    return uniqueKeyExporter;
  }

  @Override
  public long getSequenceNextVal(String sequenceName, SqlExecutor sqlExecutor) {
    AtomicLong key = new AtomicLong(0L);
    // 此处未实现步进功能，可以通过last_insert_rowid()+{num} 来实现步进
    sqlExecutor.updateAndReturnGeneratedKeys(
      "insert into " + sequenceName + "(name) values('sequence')",
      new String[]{"id"}, keys -> key.set((long) keys.get(0))
    );
    return key.get();
  }

  @Override
  public String getIdentityColumnString(int type) {
    //starts with 1, implicitly
    return "not null";
  }

  @Override
  public String getAddColumnString() {
    return "add";
  }

  @Override
  public boolean supportsNotNullWithoutDefaultValue() {
    return false;
  }
}
