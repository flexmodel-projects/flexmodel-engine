package tech.wetech.flexmodel;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tech.wetech.flexmodel.sql.JdbcDataSourceProvider;

/**
 * @author cjbi
 */
@Testcontainers
public class OracleIntegrationTests extends AbstractSessionTests {

  @Container
  public static OracleContainer container = new OracleContainer("gvenzl/oracle-xe:21-slim-faststart");

  @BeforeAll
  public static void beforeAll() {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setJdbcUrl(container.getJdbcUrl());
    dataSource.setUsername(container.getUsername());
    dataSource.setPassword(container.getPassword());
    initSession(new JdbcDataSourceProvider(dataSource));
  }
}
