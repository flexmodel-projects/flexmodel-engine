package tech.wetech.flexmodel.graphql;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import tech.wetech.flexmodel.Session;
import tech.wetech.flexmodel.SessionFactory;
import tech.wetech.flexmodel.sql.JdbcDataSourceProvider;

/**
 * @author cjbi
 */
public class AbstractIntegrationTest {

  protected static final String SCHEMA_NAME = "system";
  protected static Session session;

  @BeforeAll
  static void init() {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setJdbcUrl("jdbc:sqlite:file::memory:?cache=shared");
    JdbcDataSourceProvider jdbcDataSourceProvider = new JdbcDataSourceProvider(dataSource);
    SessionFactory sessionFactory = SessionFactory.builder()
      .setDefaultDataSourceProvider("system", jdbcDataSourceProvider)
      .build();
    session = sessionFactory.createSession(SCHEMA_NAME);
  }

  @AfterAll
  static void destroy() {
    session.close();
  }

}
