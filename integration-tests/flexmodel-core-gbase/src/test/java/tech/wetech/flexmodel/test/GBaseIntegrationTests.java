package tech.wetech.flexmodel.test;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tech.wetech.flexmodel.core.sql.JdbcDataSourceProvider;

/**
 * @author cjbi
 */
@Testcontainers
public class GBaseIntegrationTests extends AbstractSessionTests {

  @Container
  public static GBaseContainer container = new GBaseContainer<>();

  @BeforeAll
  public static void beforeAll() {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setJdbcUrl(container.getJdbcUrl());
    dataSource.setUsername(container.getUsername());
    dataSource.setPassword(container.getPassword());
    initSession(new JdbcDataSourceProvider("default", dataSource));
  }
}
