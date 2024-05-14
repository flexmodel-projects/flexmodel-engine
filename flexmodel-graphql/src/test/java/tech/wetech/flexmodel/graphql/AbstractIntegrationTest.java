package tech.wetech.flexmodel.graphql;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import tech.wetech.flexmodel.ConnectionLifeCycleManager;
import tech.wetech.flexmodel.Session;
import tech.wetech.flexmodel.SessionFactory;
import tech.wetech.flexmodel.sql.JdbcDataSourceProvider;
import tech.wetech.flexmodel.sql.JdbcMappedModels;

/**
 * @author cjbi
 */
public class AbstractIntegrationTest {

  private static final ConnectionLifeCycleManager connectionLifeCycleManager = new ConnectionLifeCycleManager();
  protected static Session session;

  @BeforeAll
  static void init() {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setJdbcUrl("jdbc:sqlite:file::memory:?cache=shared");
    connectionLifeCycleManager.addDataSourceProvider("sqlite", new JdbcDataSourceProvider(dataSource));
    SessionFactory sessionFactory = new SessionFactory(connectionLifeCycleManager, new JdbcMappedModels(dataSource));
    session = sessionFactory.createSession("sqlite");
  }

  @AfterAll
  static void destroy() {
    session.close();
  }

}
