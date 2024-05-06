package tech.wetech.flexmodel.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private final Map<String, DataSource> dataSources = new HashMap<>();
  private final Map<String, Connection> connections = new HashMap<>();

  public void addDataSource(String identifier, DataSource dataSource) {
    dataSources.put(identifier, dataSource);
  }

  public Connection getOrCreateConnection(String identifier) {
    return connections.compute(identifier, (k, v) -> {
      try {
        if (v != null && !v.isClosed()) {
          try {
            return v;
          } catch (Exception e) {
            DataSource dataSource = dataSources.get(identifier);
            return dataSource.getConnection();
          }
        }
        DataSource dataSource = dataSources.get(identifier);
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
