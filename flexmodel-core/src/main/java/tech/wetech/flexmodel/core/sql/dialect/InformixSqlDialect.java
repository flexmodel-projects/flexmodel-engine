package tech.wetech.flexmodel.core.sql.dialect;

import tech.wetech.flexmodel.core.sql.SqlExecutor;

import java.sql.Types;

/**
 * @author cjbi
 */
public class InformixSqlDialect extends SqlDialect {

  public InformixSqlDialect() {
    super();

    registerColumnType(Types.BIGINT, "int8");
    registerColumnType(Types.BINARY, "byte");
    // Informix doesn't have a bit type
    registerColumnType(Types.BIT, "smallint");
    registerColumnType(Types.CHAR, "char($l)");
    registerColumnType(Types.DATE, "date");
    registerColumnType(Types.DECIMAL, "decimal");
    registerColumnType(Types.DOUBLE, "float");
    registerColumnType(Types.FLOAT, "smallfloat");
    registerColumnType(Types.INTEGER, "integer");
    // or BYTE
    registerColumnType(Types.LONGVARBINARY, "blob");
    // or TEXT?
    registerColumnType(Types.LONGVARCHAR, "clob");
    // or MONEY
    registerColumnType(Types.NUMERIC, "decimal");
    registerColumnType(Types.REAL, "smallfloat");
    registerColumnType(Types.SMALLINT, "smallint");
    registerColumnType(Types.TIMESTAMP, "datetime year to fraction(5)");
    registerColumnType(Types.TIME, "datetime hour to second");
    registerColumnType(Types.TINYINT, "smallint");
    registerColumnType(Types.VARBINARY, "byte");
    registerColumnType(Types.VARCHAR, "varchar($l)");
    registerColumnType(Types.VARCHAR, 255, "varchar($l)");
    registerColumnType(Types.VARCHAR, 32739, "lvarchar($l)");

    registerColumnType(Types.BIGINT, "bigint");

    registerColumnType(Types.JAVA_OBJECT, "clob");
  }

  /**
   * Informix constraint name must be at the end.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public String getAddForeignKeyConstraintString(
    String constraintName,
    String[] foreignKey,
    String referencedTable,
    String[] primaryKey,
    boolean referencesPrimaryKey) {
    final StringBuilder result = new StringBuilder( 30 )
      .append( " add constraint " )
      .append( " foreign key (" )
      .append( String.join( ", ", foreignKey ) )
      .append( ") references " )
      .append( referencedTable );

    if ( !referencesPrimaryKey ) {
      result.append( " (" )
        .append( String.join( ", ", primaryKey ) )
        .append( ')' );
    }

    result.append( " constraint " ).append( constraintName );

    return result.toString();
  }

  @Override
  public String getIdentityColumnString(int type) throws DialectException {
    return (type == Types.BIGINT ? "bigserial" : "serial") + " not null";
  }

  @Override
  public boolean hasDataTypeInIdentityColumn() {
    return false;
  }

  @Override
  public boolean supportsCommentOn() {
    return true;
  }

  @Override
  public String getAddColumnString() {
    return "add";
  }

  @Override
  public String getDropColumnString() {
    return "drop";
  }

  @Override
  public String getLimitString(String sql, String offsetPlaceholder, String limitPlaceHolder) {
    return "select * from (select "
           + (offsetPlaceholder != null ? "skip " + offsetPlaceholder + " first " + limitPlaceHolder : " first " + limitPlaceHolder)
           + " * from (" + sql + "))";
  }

  @Override
  public long getSequenceNextVal(String sequenceName, SqlExecutor sqlExecutor) {
    return sqlExecutor.queryForScalar("select " + sequenceName + ".nextval" + " from informix.systables where tabid=1", Long.class);
  }

  @Override
  public String toBooleanValueString(boolean bool) {
    return bool ? "'t'" : "'f'";
  }

}
