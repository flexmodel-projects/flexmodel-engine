package tech.wetech.flexmodel;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.zaxxer.hikari.HikariDataSource;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MongoDBContainer;
import tech.wetech.flexmodel.mongodb.MongoDataSourceProvider;
import tech.wetech.flexmodel.sql.JdbcDataSourceProvider;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * @author cjbi
 */
public abstract class AbstractSessionIntegrationTest extends AbstractIntegrationTest {

  public static final MapMappedModels MODEL_MAP = new MapMappedModels();
  private static final ConnectionLifeCycleManager CONNECTION_LIFE_CYCLE_MANAGER = new ConnectionLifeCycleManager();
  protected Session session;

  @BeforeAll
  static void init() {
    acceptCommandAsync((key, container) -> container.start());
    acceptCommand((key, container) -> {
      if (container instanceof JdbcDatabaseContainer<?> jdbcDatabaseContainer) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcDatabaseContainer.getJdbcUrl());
        dataSource.setUsername(jdbcDatabaseContainer.getUsername());
        dataSource.setPassword(jdbcDatabaseContainer.getPassword());
        CONNECTION_LIFE_CYCLE_MANAGER.addDataSourceProvider(key, new JdbcDataSourceProvider(dataSource));
      } else if (container instanceof MongoDBContainer mongoDBContainer) {
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
          fromProviders(PojoCodecProvider.builder().automatic(true).build()));
        MongoClient mongoClient = MongoClients.create(mongoDBContainer.getConnectionString());
        MongoDatabase database = mongoClient.getDatabase("test")
          .withCodecRegistry(pojoCodecRegistry);
        CONNECTION_LIFE_CYCLE_MANAGER.addDataSourceProvider(key, new MongoDataSourceProvider(database));
      }
    });
    String currentDb = System.getProperty("current_db");
    if (currentDb != null && currentDb.contains("sqlite")) {
      HikariDataSource dataSource = new HikariDataSource();
      dataSource.setJdbcUrl("jdbc:sqlite:file::memory:?cache=shared");
      CONNECTION_LIFE_CYCLE_MANAGER.addDataSourceProvider("sqlite", new JdbcDataSourceProvider(dataSource));
    }
  }

  @BeforeEach
  void setUp() {
    SessionFactory sessionFactory = SessionFactory.builder()
      .setMappedModels(MODEL_MAP)
      .setConnectionLifeCycleManager(CONNECTION_LIFE_CYCLE_MANAGER)
      .build();
    MultiDbSessionDelegates delegates = new MultiDbSessionDelegates();
    acceptCommand((key, container) -> delegates.addDelegate(key, sessionFactory.createSession(key)));
    String currentDb = System.getProperty("current_db");
    if (currentDb != null && currentDb.contains("sqlite")) {
      delegates.addDelegate("sqlite", sessionFactory.createSession("sqlite"));
    }
    session = delegates;
  }

  @AfterEach
  void tearDown() {
  }

  @AfterAll
  static void destroy() {
    acceptCommand((key, container) -> container.stop());
    CONNECTION_LIFE_CYCLE_MANAGER.destroy();
  }

}
