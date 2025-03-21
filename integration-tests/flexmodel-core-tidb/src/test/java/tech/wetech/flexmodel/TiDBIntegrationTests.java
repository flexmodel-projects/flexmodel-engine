package tech.wetech.flexmodel;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.tidb.TiDBContainer;
import tech.wetech.flexmodel.sql.JdbcDataSourceProvider;

/**
 * @author cjbi
 */
@Testcontainers
public class TiDBIntegrationTests extends AbstractSessionTests {

  @Container
  public static TiDBContainer container = new TiDBContainer("pingcap/tidb:v7.1.5");

  @BeforeAll
  public static void beforeAll() {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setJdbcUrl(container.getJdbcUrl());
    dataSource.setUsername(container.getUsername());
    dataSource.setPassword(container.getPassword());
    initSession(new JdbcDataSourceProvider("default", dataSource));
  }
}
