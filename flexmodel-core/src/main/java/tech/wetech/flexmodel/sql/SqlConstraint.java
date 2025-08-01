package tech.wetech.flexmodel.sql;

import tech.wetech.flexmodel.sql.dialect.DialectException;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * A relational constraint.
 *
 * @author cjbi@outlook.com
 */
abstract class SqlConstraint implements Exportable {
  private final List<SqlColumn> sqlColumns = new ArrayList<>();
  private String name;
  private SqlTable sqlTable;

  /**
   * Helper method for {@link #generateName(String, SqlTable, SqlColumn...)}.
   *
   * @return String The generated name
   */
  public static String generateName(String prefix, SqlTable table, List<SqlColumn> columns) {
    //N.B. legacy APIs are involved: can't trust that the columns List is actually
    //containing Column instances - the generic type isn't consistently enforced.
    ArrayList<SqlColumn> defensive = new ArrayList<>(columns.size());
    for (Object o : columns) {
      if (o instanceof SqlColumn) {
        defensive.add((SqlColumn) o);
      }
      //else: others might be Formula instances. They don't need to be part of the name generation.
    }
    return generateName(prefix, table, defensive.toArray(new SqlColumn[0]));
  }

  /**
   * If a constraint is not explicitly named, this is called to generate
   * a unique hash using the table and column names.
   * Static so the name can be generated prior to creating the Constraint.
   * They're cached, keyed by name, in multiple locations.
   *
   * @return String The generated name
   */
  public static String generateName(String prefix, SqlTable table, SqlColumn... columns) {
    // Use a concatenation that guarantees uniqueness, even if identical names
    // exist between all table and column identifiers.

    StringBuilder sb = new StringBuilder("table`" + table.getName() + "`");

    // Ensure a consistent ordering of columns, regardless of the order
    // they were bound.
    // Clone the list, as sometimes a set of order-dependent Column
    // bindings are given.
    SqlColumn[] alphabeticalColumns = columns.clone();
    Arrays.sort(alphabeticalColumns, ColumnComparator.INSTANCE);
    for (SqlColumn column : alphabeticalColumns) {
      String columnName = column == null ? "" : column.getName();
      sb.append("column`").append(columnName).append("`");
    }
    return prefix + hashedName(sb.toString());
  }

  /**
   * Hash a constraint name using MD5. Convert the MD5 digest to base 35
   * (full alphanumeric), guaranteeing
   * that the length of the name will always be smaller than the 30
   * character identifier restriction enforced by a few dialects.
   *
   * @param s The name to be hashed.
   * @return String The hashed name.
   */
  public static String hashedName(String s) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.reset();
      md.update(s.getBytes());
      byte[] digest = md.digest();
      BigInteger bigInt = new BigInteger(1, digest);
      // By converting to base 35 (full alphanumeric), we guarantee
      // that the length of the name will always be smaller than the 30
      // character identifier restriction enforced by a few dialects.
      return bigInt.toString(35);
    } catch (NoSuchAlgorithmException e) {
      throw new DialectException("Unable to generate a hashed Constraint name!", e);
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void addColumn(SqlColumn sqlColumn) {
    if (!sqlColumns.contains(sqlColumn)) {
      sqlColumns.add(sqlColumn);
    }
  }

  public void addColumns(Iterator columnIterator) {
    while (columnIterator.hasNext()) {
      SqlColumn col = (SqlColumn) columnIterator.next();
      addColumn(col);
    }
  }

  /**
   * @return true if this constraint already contains a column with same name.
   */
  public boolean containsColumn(SqlColumn column) {
    return sqlColumns.contains(column);
  }

  public int getColumnSpan() {
    return sqlColumns.size();
  }

  public List<SqlColumn> getColumns() {
    return sqlColumns;
  }
//TODO
//    public abstract String sqlConstraintString(
//        SqlStringGenerationContext context,
//        String constraintName,
//        String defaultCatalog,
//        String defaultSchema);
//TODO
//    /**
//     * @return String The prefix to use in generated constraint names.  Examples:
//     * "UK_", "FK_", and "PK_".
//     */
//    public abstract String generatedConstraintNamePrefix();

  public Iterator<SqlColumn> getColumnIterator() {
    return sqlColumns.iterator();
  }

  public SqlTable getTable() {
    return sqlTable;
  }

  public void setTable(SqlTable sqlTable) {
    this.sqlTable = sqlTable;
  }

  @Override
  public String toString() {
    return getClass().getName() + '(' + getTable().getName() + getColumns() + ") as " + name;
  }

  private static class ColumnComparator implements Comparator<SqlColumn> {
    public static ColumnComparator INSTANCE = new ColumnComparator();

    public int compare(SqlColumn col1, SqlColumn col2) {
      return col1.getName().compareTo(col2.getName());
    }
  }


}
