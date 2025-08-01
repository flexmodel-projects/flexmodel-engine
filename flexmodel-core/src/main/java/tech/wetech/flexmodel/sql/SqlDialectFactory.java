package tech.wetech.flexmodel.sql;

import tech.wetech.flexmodel.sql.dialect.*;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Locale;

/**
 * @author cjbi
 */
public class SqlDialectFactory {

  public static SqlDialect create(DatabaseMetaData databaseMetaData) {
    String databaseProductName;
    try {
      databaseProductName = databaseMetaData.getDatabaseProductName();
    } catch (SQLException e) {
      throw new RuntimeException("while detecting database product", e);
    }
    final String quoteString = getIdentifierQuoteString(databaseMetaData);
    SqlDialect sqlDialect = getSqlDialect(databaseProductName);
    sqlDialect.setIdentifierQuoteString(quoteString);
    return sqlDialect;
  }

  private static SqlDialect getSqlDialect(String databaseProductName) {
    final String upperProductName = databaseProductName.toUpperCase(Locale.ROOT).trim();
    SqlDialect sqlDialect = switch (upperProductName) {
      case "MYSQL" -> new MySQLSqlDialect();
      case "POSTGRESQL" -> new PostgreSQLSqlDialect();
      case "MICROSOFT SQL SERVER" -> new SQLServerSqlDialect();
      case "ORACLE" -> new OracleSqlDialect();
      case "MARIADB" -> new MariaDBSqlDialect();
      case "SQLITE" -> new SQLiteSqlDialect();
      default -> null;
    };
    if (sqlDialect == null) {
      // Now the fuzzy matches.
      if (upperProductName.startsWith("DB2")) {
        sqlDialect = new DB2SqlDialect();
      } else if (upperProductName.startsWith("INFORMIX")) {
        sqlDialect = new InformixSqlDialect();
      } else if (upperProductName.startsWith("GBASE")) {
        sqlDialect = new GBaseSqlDialect();
      } else if (upperProductName.startsWith("DM")) {
        sqlDialect = new DmSqlDialect();
      } else {
        throw new IllegalStateException("Unexpected value: " + upperProductName);
      }
    }
    return sqlDialect;
  }

  private static String getIdentifierQuoteString(DatabaseMetaData databaseMetaData) {
    try {
      return databaseMetaData.getIdentifierQuoteString();
    } catch (SQLException e) {
      throw new IllegalArgumentException("cannot deduce identifier quote string", e);
    }
  }

}
