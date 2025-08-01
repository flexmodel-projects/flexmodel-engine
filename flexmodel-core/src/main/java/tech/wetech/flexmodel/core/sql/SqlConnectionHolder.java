package tech.wetech.flexmodel.core.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.core.DataSourceProvider;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cjbi
 */
public class SqlConnectionHolder {

  Logger log = LoggerFactory.getLogger(SqlConnectionHolder.class);

  private final Map<String, DataSourceProvider> dataSourceProviderMap;
  private final Map<String, Connection> connections = new HashMap<>();

  public SqlConnectionHolder(Map<String, DataSourceProvider> dataSourceProviderMap) {
    this.dataSourceProviderMap = dataSourceProviderMap;
  }

  public Connection getOrCreateConnection(String identifier) {
    return connections.compute(identifier, (k, v) -> {
      try {
        if (v != null && !v.isClosed()) {
          try {
            return v;
          } catch (Exception e) {
            DataSource dataSource = ((JdbcDataSourceProvider) dataSourceProviderMap.get(identifier)).dataSource();
            return dataSource.getConnection();
          }
        }
        DataSource dataSource = ((JdbcDataSourceProvider) dataSourceProviderMap.get(identifier)).dataSource();
        return dataSource.getConnection();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    });
  }


  public void destroy() {
    for (Map.Entry<String, Connection> entry : connections.entrySet()) {
      closeConnection(entry.getValue());
    }
    connections.clear();
  }

  private void closeConnection(Connection conn) {
    if (conn != null) {
      try {
        conn.close();
      } catch (SQLException ex) {
        log.debug("Could not close JDBC Connection", ex);
      } catch (Throwable ex) {
        // We don't trust the JDBC driver: It might throw RuntimeException or Error.
        log.debug("Unexpected exception on closing JDBC Connection", ex);
      }
    }
  }

}
