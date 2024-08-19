package tech.wetech.flexmodel;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import tech.wetech.flexmodel.sql.JdbcDataSourceProvider;

@Path("/hello")
public class GreetingResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello from Quarkus REST";
    }

  @Singleton
  public SessionFactory sessionFactory() {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setMaxLifetime(30000); // 30s
    dataSource.setJdbcUrl("jdbc:sqlite:file::memory:?cache=shared");
    return SessionFactory.builder()
      .setDefaultDataSourceProvider("system", new JdbcDataSourceProvider(dataSource))
      .build();
  }

}
