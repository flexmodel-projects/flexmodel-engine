package tech.wetech.flexmodel.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import tech.wetech.flexmodel.session.SessionFactory;
import tech.wetech.flexmodel.session.SessionManager;
import tech.wetech.flexmodel.sql.JdbcDataSourceProvider;

/**
 * Session配置类
 * 配置SessionFactory和SessionManager
 *
 * @author cjbi
 */
@ApplicationScoped
public class SessionConfig {

  /**
   * 配置SessionFactory
   */
  @Produces
  @ApplicationScoped
  public SessionFactory sessionFactory() {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setMaxLifetime(30000); // 30s
    dataSource.setJdbcUrl("jdbc:sqlite:file::memory:?cache=shared");

    return SessionFactory.builder()
      .setDefaultDataSourceProvider(new JdbcDataSourceProvider("system", dataSource))
      .build();
  }

  /**
   * 配置通用的SessionManager（可选）
   * 如果不使用QuarkusSessionManager，可以使用这个
   */
  @Produces
  @ApplicationScoped
  public SessionManager sessionManager(SessionFactory sessionFactory) {
    return new SessionManager(sessionFactory);
  }
}
