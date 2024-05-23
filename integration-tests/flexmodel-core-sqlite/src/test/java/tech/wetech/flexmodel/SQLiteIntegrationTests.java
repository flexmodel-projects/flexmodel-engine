package tech.wetech.flexmodel;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeAll;
import tech.wetech.flexmodel.sql.JdbcDataSourceProvider;

/**
 * @author cjbi
 */
public class SQLiteIntegrationTests extends AbstractSessionTests {

  @BeforeAll
  public static void beforeAll() {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setJdbcUrl("jdbc:sqlite:file::memory:?cache=shared");
    initSession(new JdbcDataSourceProvider(dataSource));
  }
}
