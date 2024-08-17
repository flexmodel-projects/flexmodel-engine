package tech.wetech.flexmodel.codegen;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * @author cjbi
 */
public class ConnectionWrapper implements DataSource {

  private final String url;
  private final String username;
  private final String password;

  public ConnectionWrapper(String url, String username, String password) {
    this.url = url;
    this.username = username;
    this.password = password;
  }

  @Override
  public Connection getConnection() throws SQLException {
    return DriverManager.getConnection(url, username, password);
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public PrintWriter getLogWriter() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void setLogWriter(PrintWriter out) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void setLoginTimeout(int seconds) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public int getLoginTimeout() throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
