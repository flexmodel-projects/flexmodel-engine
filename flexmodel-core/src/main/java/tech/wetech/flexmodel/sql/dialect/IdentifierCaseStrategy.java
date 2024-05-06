package tech.wetech.flexmodel.sql.dialect;

/**
 * An enumeration of the way DatabaseMetaData might store and return identifiers
 *
 * @author cjbi
 */
public enum IdentifierCaseStrategy {
  /**
   * The identifier is stored in mixed case.
   *
   * @see java.sql.DatabaseMetaData#storesMixedCaseIdentifiers()
   * @see java.sql.DatabaseMetaData#storesMixedCaseQuotedIdentifiers()
   */
  MIXED,
  /**
   * The identifier is stored in upper case.
   *
   * @see java.sql.DatabaseMetaData#storesUpperCaseIdentifiers()
   * @see java.sql.DatabaseMetaData#storesUpperCaseQuotedIdentifiers()
   */
  UPPER,
  /**
   * The identifier is stored in lower case.
   *
   * @see java.sql.DatabaseMetaData#storesLowerCaseIdentifiers()
   * @see java.sql.DatabaseMetaData#storesLowerCaseQuotedIdentifiers()
   */
  LOWER
}
