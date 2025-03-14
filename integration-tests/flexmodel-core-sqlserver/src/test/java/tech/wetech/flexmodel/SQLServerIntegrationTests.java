package tech.wetech.flexmodel;

import com.zaxxer.hikari.HikariDataSource;
import org.testcontainers.containers.MSSQLServerContainer;
import tech.wetech.flexmodel.sql.JdbcDataSourceProvider;

/**
 * @author cjbi
 */
// @Testcontainers
public class SQLServerIntegrationTests extends AbstractSessionTests {

  // @Container
  public static MSSQLServerContainer container = new MSSQLServerContainer().acceptLicense();

  // @BeforeAll
  public static void beforeAll() {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setJdbcUrl(container.getJdbcUrl());
    dataSource.setUsername(container.getUsername());
    dataSource.setPassword(container.getPassword());
    initSession(new JdbcDataSourceProvider("default", dataSource));
  }
}
