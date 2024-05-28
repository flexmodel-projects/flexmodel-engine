package tech.wetech.flexmodel;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.sql.JdbcDataSourceProvider;
import tech.wetech.flexmodel.sql.JdbcMappedModels;

import java.util.Map;

/**
 * @author cjbi
 */
@Configuration
public class EngineConfig {

  @Bean
  public Session session(SessionFactory sessionFactory) {
    Session session = sessionFactory.createSession("default");
    if (session.getModel("Student") == null) {
      session.createEntity("Student", entity -> entity
        .addField(new IDField("id").setGeneratedValue(IDField.DefaultGeneratedValue.STRING_NO_GEN))
        .addField(new StringField("name"))
      );
    }
    session.insert("Student", Map.of("id", "001", "name", "张三"));
    return session;
  }

  @Bean
  public SessionFactory sessionFactory(ConnectionLifeCycleManager connectionLifeCycleManager) {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setJdbcUrl("jdbc:sqlite:file::memory:?cache=shared");
    connectionLifeCycleManager.addDataSourceProvider("default", new JdbcDataSourceProvider(dataSource));
    return SessionFactory.builder()
      .setConnectionLifeCycleManager(connectionLifeCycleManager)
      .setMappedModels(new JdbcMappedModels(dataSource))
      .build();
  }

  @Bean
  public ConnectionLifeCycleManager connectionLifeCycleManager() {
    return new ConnectionLifeCycleManager();
  }

}
