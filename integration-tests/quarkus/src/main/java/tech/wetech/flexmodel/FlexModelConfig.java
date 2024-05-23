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
  public SessionFactory sessionFactory(ConnectionLifeCycleManager connectionLifeCycleManager) {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setJdbcUrl("jdbc:sqlite:file::memory:?cache=shared");
    connectionLifeCycleManager.addDataSourceProvider("default", new JdbcDataSourceProvider(dataSource));
    return SessionFactory.builder()
      .setConnectionLifeCycleManager(connectionLifeCycleManager)
      .setMappedModels(new JdbcMappedModels(dataSource))
      .build();
  }

  @Produces
  public ConnectionLifeCycleManager connectionLifeCycleManager() {
    return new ConnectionLifeCycleManager();
  }

}
