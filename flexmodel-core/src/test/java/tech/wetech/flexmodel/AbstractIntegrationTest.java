package tech.wetech.flexmodel;

import org.testcontainers.containers.*;
import org.testcontainers.tidb.TiDBContainer;
import org.testcontainers.utility.DockerImageName;
import tech.wetech.flexmodel.containers.DmContainer;
import tech.wetech.flexmodel.containers.GBaseContainer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @author cjbi
 */
public abstract class AbstractIntegrationTest {

  protected static final Map<String, GenericContainer<?>> DB_CONTAINERS = new HashMap<>();

  static {
    // these are slowly
    //   DB_CONTAINERS.put(Db2Container.NAME, new Db2Container().acceptLicense().withUrlParam("progressiveStreaming", "2"));
//    DB_CONTAINERS.put("informix", new InformixContainer());
    DB_CONTAINERS.put(MySQLContainer.NAME, new MySQLContainer<>("mysql:8.0"));
    DB_CONTAINERS.put(MariaDBContainer.NAME, new MariaDBContainer<>(DockerImageName.parse("mariadb:10.5.5")));
    DB_CONTAINERS.put(OracleContainer.NAME, new OracleContainer("gvenzl/oracle-xe:21-slim-faststart"));
    DB_CONTAINERS.put(PostgreSQLContainer.NAME, new PostgreSQLContainer<>(DockerImageName.parse("postgis/postgis:16-3.4-alpine").asCompatibleSubstituteFor("postgres")));
    DB_CONTAINERS.put(MSSQLServerContainer.NAME, new MSSQLServerContainer().acceptLicense());
    DB_CONTAINERS.put("mongodb", new MongoDBContainer("mongo:5.0.0"));
    DB_CONTAINERS.put(GBaseContainer.NAME, new GBaseContainer<>());
    DB_CONTAINERS.put(DmContainer.NAME, new DmContainer<>());
    DB_CONTAINERS.put("tidb", new TiDBContainer("pingcap/tidb:v7.1.5"));
  }

  protected static void acceptCommand(BiConsumer<String, GenericContainer<?>> consumer) {
    // 从vm opts中获取需要测试的数据库，如果不存在则全部测试
    String currentDb = System.getProperty("current_db");
    DB_CONTAINERS.forEach((key, container) -> {
      if (currentDb != null) {
        if (currentDb.contains(key)) {
          consumer.accept(key, container);
        }
      } else {
        consumer.accept(key, container);
      }
    });
  }

  protected static void acceptCommandAsync(BiConsumer<String, GenericContainer<?>> consumer) {
    // 从vm opts中获取需要测试的数据库，如果不存在则全部测试
    String currentDb = System.getProperty("current_db");
    DB_CONTAINERS.entrySet().parallelStream().forEach(entry -> {
      String key = entry.getKey();
      GenericContainer<?> container = entry.getValue();
      if (currentDb != null) {
        if (currentDb.contains(key)) {
          consumer.accept(key, container);
        }
      } else {
        consumer.accept(key, container);
      }
    });
  }

}
