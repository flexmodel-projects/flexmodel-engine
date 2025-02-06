package tech.wetech.flexmodel;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.Db2Container;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tech.wetech.flexmodel.sql.JdbcDataSourceProvider;

/**
 * @author cjbi
 */
@Testcontainers
public class DB2IntegrationTests extends AbstractSessionTests {

  @Container
  public static Db2Container container = new Db2Container().acceptLicense().withUrlParam("progressiveStreaming", "2");

  @BeforeAll
  public static void beforeAll() {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setJdbcUrl(container.getJdbcUrl());
    dataSource.setUsername(container.getUsername());
    dataSource.setPassword(container.getPassword());
    initSession(new JdbcDataSourceProvider("default", dataSource));
  }
}
