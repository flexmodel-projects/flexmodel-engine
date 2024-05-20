package tech.wetech.flexmodel;

import com.zaxxer.hikari.HikariDataSource;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import tech.wetech.flexmodel.sql.JdbcDataSourceProvider;
import tech.wetech.flexmodel.sql.JdbcMappedModels;

/**
 * @author cjbi
 */
@Dependent
@Startup
public class FlexModelConfig {

  @Produces
  public SessionFactory sessionFactory() {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setJdbcUrl("jdbc:sqlite:file::memory:?cache=shared");
    ConnectionLifeCycleManager connectionLifeCycleManager = new ConnectionLifeCycleManager();
    connectionLifeCycleManager.addDataSourceProvider("sqlite", new JdbcDataSourceProvider(dataSource));
    return SessionFactory.builder()
      .setConnectionLifeCycleManager(connectionLifeCycleManager)
      .setMappedModels(new JdbcMappedModels(dataSource))
      .build();
  }
}
