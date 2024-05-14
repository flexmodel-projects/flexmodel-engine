package tech.wetech.flexmodel.sql.dialect;

import tech.wetech.flexmodel.sql.*;

import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * @author cjbi@outlook.com
 */
public abstract class SqlDialect {

  private final TypeNames typeNames = new TypeNames();

  private Map<String, SqlFunction> sqlFunctions = new HashMap<>();

  public SqlDialect() {
    registerColumnType(Types.BIT, "bit");
    registerColumnType(Types.BOOLEAN, "boolean");
    registerColumnType(Types.TINYINT, "tinyint");
    registerColumnType(Types.SMALLINT, "smallint");
    registerColumnType(Types.INTEGER, "integer");
    registerColumnType(Types.BIGINT, "bigint");
    registerColumnType(Types.FLOAT, "float($p)");
    registerColumnType(Types.DOUBLE, "double precision");
    registerColumnType(Types.NUMERIC, "numeric($p,$s)");
    registerColumnType(Types.REAL, "real");

    registerColumnType(Types.DATE, "date");
    registerColumnType(Types.TIME, "time");
    registerColumnType(Types.TIMESTAMP, "timestamp");

    registerColumnType(Types.VARBINARY, "bit varying($l)");
    registerColumnType(Types.LONGVARBINARY, "bit varying($l)");
    registerColumnType(Types.BLOB, "blob");

    registerColumnType(Types.CHAR, "char($l)");
    registerColumnType(Types.VARCHAR, "varchar($l)");
    registerColumnType(Types.LONGVARCHAR, "varchar($l)");
    registerColumnType(Types.CLOB, "clob");

    registerColumnType(Types.NCHAR, "nchar($l)");
    registerColumnType(Types.NVARCHAR, "nvarchar($l)");
    registerColumnType(Types.LONGNVARCHAR, "nvarchar($l)");
    registerColumnType(Types.NCLOB, "nclob");

    // standard sql92 functions
    registerFunction("count", args -> "count(" + args[0] + ")", 1);
    registerFunction("avg", args -> "avg(" + args[0] + ")", 1);
    registerFunction("sum", args -> "sum(" + args[0] + ")", 1);
    registerFunction("max", args -> "max(" + args[0] + ")", 1);
    registerFunction("min", args -> "min(" + args[0] + ")", 1);
  }


  private String identifierQuoteString = "";

  public void setIdentifierQuoteString(String identifierQuoteString) {
    this.identifierQuoteString = identifierQuoteString;
  }

  protected final StandardTableExporter standardTableExporter = new StandardTableExporter(this);
  protected final StandardViewExporter standardViewExporter = new StandardViewExporter(this);
  protected final StandardColumnExporter standardColumnExporter = new StandardColumnExporter(this);
  protected final StandardIndexExporter standardIndexExporter = new StandardIndexExporter(this);
  protected final StandardUniqueKeyExporter standardUniqueKeyExporter = new StandardUniqueKeyExporter(this);
  protected final StandardForeignKeyExporter standardForeignKeyExporter = new StandardForeignKeyExporter(this);
  protected final SequenceExporter standardSequenceExporter = new StandardSequenceExporter(this);

  public StandardTableExporter getTableExporter() {
    return standardTableExporter;
  }

  public StandardViewExporter getViewExporter() {
    return standardViewExporter;
  }

  public StandardColumnExporter getColumnExporter() {
    return standardColumnExporter;
  }

  public StandardIndexExporter getIndexExporter() {
    return standardIndexExporter;
  }

  public StandardUniqueKeyExporter getUniqueKeyExporter() {
    return standardUniqueKeyExporter;
  }

  public StandardForeignKeyExporter getForeignKeyExporter() {
    return standardForeignKeyExporter;
  }

  public SequenceExporter getSequenceExporter() {
    return standardSequenceExporter;
  }

  /**
   * Subclasses register a type name for the given type code and maximum
   * column length. <tt>$l</tt> in the type name with be replaced by the
   * column length (if appropriate).
   *
   * @param code     The {@link Types} typecode
   * @param capacity The maximum length of database type
   * @param name     The database type name
   */
  protected void registerColumnType(int code, long capacity, String name) {
    typeNames.put(code, capacity, name);
  }

  /**
   * Subclasses register a type name for the given type code. <tt>$l</tt> in
   * the type name with be replaced by the column length (if appropriate).
   *
   * @param code The {@link Types} typecode
   * @param name The database type name
   */
  protected void registerColumnType(int code, String name) {
    typeNames.put(code, name);
  }

  protected void registerFunction(String name, Function<String[], String> fragment, int arguments) {
    // HHH-7721: SQLFunctionRegistry expects all lowercase.  Enforce,
    // just in case a user's customer dialect uses mixed cases.
    sqlFunctions.put(name.toLowerCase(Locale.ROOT), new SqlFunction(fragment, arguments));
  }

  protected void registerFunction(String name, Function<String[], String> fragment) {
    // HHH-7721: SQLFunctionRegistry expects all lowercase.  Enforce,
    // just in case a user's customer dialect uses mixed cases.
    sqlFunctions.put(name.toLowerCase(Locale.ROOT), new SqlFunction(fragment));
  }


  /**
   * Get the name of the database type associated with the given
   * {@link Types} typecode with the given storage specification
   * parameters.
   *
   * @param code      The {@link Types} typecode
   * @param length    The datatype length
   * @param precision The datatype precision
   * @param scale     The datatype scale
   * @return the database type name
   * @throws DialectException If no mapping was specified for that type.
   */
  public String getTypeName(int code, long length, int precision, int scale) throws DialectException {
    final String result = typeNames.get(code, length, precision, scale);
    if (result == null) {
      throw new DialectException(
        String.format("No type mapping for java.sql.Types code: %s, length: %s", code, length)
      );
    }
    return result;
  }

  /**
   * Get the comment into a form supported for column definition.
   *
   * @param comment The comment to apply
   * @return The comment fragment
   */
  public String getColumnComment(String comment) {
    return "";
  }

  /**
   * The keyword used to specify a nullable column.
   *
   * @return String
   */
  public String getNullColumnString() {
    return "";
  }

  /**
   * The subcommand of the {@code alter table} command used to add
   * a column to a table, usually {@code add column} or {@code add}.
   *
   * @return The {@code add column} fragment.
   */
  public String getAddColumnString() {
    return "add column";
  }

  /**
   * The syntax for the suffix used to add a column to a table (optional).
   *
   * @return The suffix "add column" fragment.
   */
  public String getAddColumnSuffixString() {
    return "";
  }

  /**
   * Command used to alter a table.
   *
   * @param tableName The name of the table to alter
   * @return The command used to alter a table.
   * @since 1.0.0
   */
  public String getAlterTableString(String tableName) {
    final StringBuilder sb = new StringBuilder("alter table ");
    if (supportsIfExistsAfterAlterTable()) {
      sb.append("if exists ");
    }
    sb.append(tableName);
    return sb.toString();
  }

  /**
   * The syntax used to modify a column to a table (optional).
   *
   * @return The "drop column" fragment.
   */
  public String getDropColumnString() {
    return "drop column";
  }

  /**
   * For an "alter table", can the phrase "if exists" be applied?
   *
   * @return {@code true} if the "if exists" can be applied after ALTER TABLE
   * @since 1.0.0
   */
  public boolean supportsIfExistsAfterAlterTable() {
    return false;
  }

  /**
   * For dropping a table, can the phrase "if exists" be applied before the table name?
   * <p/>
   * NOTE : Only one or the other (or neither) of this and {@link #supportsIfExistsAfterTableName} should return true
   *
   * @return {@code true} if the "if exists" can be applied before the table name
   */
  public boolean supportsIfExistsBeforeTableName() {
    return false;
  }

  /**
   * For dropping a table, can the phrase "if exists" be applied after the table name?
   * <p/>
   * NOTE : Only one or the other (or neither) of this and {@link #supportsIfExistsBeforeTableName} should return true
   *
   * @return {@code true} if the "if exists" can be applied after the table name
   */
  public boolean supportsIfExistsAfterTableName() {
    return false;
  }

  /**
   * Completely optional cascading drop clause
   *
   * @return String
   */
  public String getCascadeConstraintsString() {
    return "";
  }

  /**
   * Command used to create a table.
   *
   * @return The command used to create a table.
   */
  public String getCreateTableString() {
    return "create table";
  }

  public String getIdentityColumnString(int type) throws DialectException {
    return "";
  }

  public boolean hasDataTypeInIdentityColumn() {
    return true;
  }

  public boolean supportsAutoIncrementColumn() {
    return true;
  }

  public String getTableTypeString() {
    // grrr... for differentiation of mysql storage engines
    return "";
  }

  public String quoteIdentifier(String identifier) {
    return identifierQuoteString + identifier + identifierQuoteString;
  }

  public String getIdentifierQuoteString() {
    return identifierQuoteString;
  }

  public String getTableComment(String comment) {
    return "";
  }

  /**
   * Do we need to qualify index names with the schema name?
   *
   * @return boolean
   */
  public boolean supportsQualifyIndexName() {
    return true;
  }

  /**
   * Do we need to drop constraints before dropping tables in this dialect?
   *
   * @return True if constraints must be dropped prior to dropping
   * the table; false otherwise.
   */
  public boolean dropConstraints() {
    return true;
  }

  /**
   * Does this dialect/database support commenting on tables, columns, etc?
   *
   * @return {@code true} if commenting is supported
   */
  public boolean supportsCommentOn() {
    return false;
  }

  public IdentifierCaseStrategy storesIdentifierCaseStrategy() {
    return IdentifierCaseStrategy.UPPER;
  }

  public boolean supportsIfExistsBeforeConstraintName() {
    return false;
  }

  public boolean supportsIfExistsAfterConstraintName() {
    return false;
  }

  public boolean supportsTableCheck() {
    return true;
  }

  public String getAddForeignKeyConstraintString(
    String constraintName,
    String[] foreignKey,
    String referencedTable,
    String[] primaryKey,
    boolean referencesPrimaryKey) {
    final StringBuilder res = new StringBuilder(30);

    res.append(" add constraint ")
      .append(quoteIdentifier(constraintName))
      .append(" foreign key (")
      .append(String.join(", ", foreignKey))
      .append(") references ")
      .append(referencedTable);

    if (!referencesPrimaryKey) {
      res.append(" (")
        .append(String.join(", ", primaryKey))
        .append(')');
    }
    return res.toString();
  }

  public boolean supportsCascadeDelete() {
    return true;
  }

  public String getDropForeignKeyString() {
    return " drop constraint ";
  }

  /**
   * 将sql变成分页sql语句,提供将offset及limit使用占位符号(placeholder)替换.
   * <pre>
   * 如mysql
   * dialect.getLimitString("select * from user",":offset",":limit") 将返回
   * select * from user limit :offset,:limit
   * </pre>
   *
   * @param sql               实际SQL语句
   * @param offsetPlaceholder 分页开始纪录条数－占位符号
   * @param limitPlaceHolder  分页纪录条数占位符号
   * @return 包含占位符的分页sql
   */
  public String getLimitString(String sql, String offsetPlaceholder, String limitPlaceHolder) {
    return getLimitOffsetRowsString(sql, offsetPlaceholder, limitPlaceHolder);
  }

  protected String getLimitOffsetRowsString(String sql, String offset, String limit) {
    StringBuilder builder = new StringBuilder(sql);
    if (limit != null) {
      builder.append(" limit ").append(limit);
    }
    if (offset != null) {
      builder.append(" offset ").append(offset);
    }
    return builder.toString();
  }

  protected String getOffsetFetchRowsString(String sql, String offset, String limit) {
    StringBuilder builder = new StringBuilder(sql);
    if (offset != null) {
      builder.append(" offset ").append(offset).append(" rows");
    }
    if (limit != null) {
      builder.append(" fetch first ").append(limit).append(" rows only");
    }
    return builder.toString();
  }

  public String getCountString(String sql) {
    return "select count(*) from ( " + sql + ") tmp_count";
  }

  public long getSequenceNextVal(String sequenceName, SqlExecutor sqlExecutor) {
    return sqlExecutor.queryForScalar("select " + quoteIdentifier(sequenceName) + ".nextval from dual", Long.class);
  }

  public String doExtractConstraintName(SQLException ex) {
    return null;
  }

  public String getGeneratedKeyName(String name) {
    return quoteIdentifier(name);
  }

  /**
   * The SQL literal value to which this database maps boolean values.
   *
   * @param bool The boolean value
   * @return The appropriate SQL literal.
   */
  public String toBooleanValueString(boolean bool) {
    return bool ? "1" : "0";
  }

  public String getFunctionString(String name, String... argumentsPlaceHolder) {
    return sqlFunctions.get(name).render(argumentsPlaceHolder);
  }

  public boolean supportsGroupByColumnAlias() {
    return true;
  }

  public boolean supportsJSR310() {
    return true;
  }

  public boolean supportsNotNullWithoutDefaultValue() {
    return true;
  }

  public boolean useFirstGeneratedId() {
    return false;
  }

}
