package tech.wetech.flexmodel;

import com.mongodb.client.MongoDatabase;
import tech.wetech.flexmodel.cache.Cache;
import tech.wetech.flexmodel.cache.CachingMappedModels;
import tech.wetech.flexmodel.mongodb.MongoContext;
import tech.wetech.flexmodel.mongodb.MongoDataSourceProvider;
import tech.wetech.flexmodel.mongodb.MongoSession;
import tech.wetech.flexmodel.sql.JdbcDataSourceProvider;
import tech.wetech.flexmodel.sql.NamedParameterSqlExecutor;
import tech.wetech.flexmodel.sql.SqlContext;
import tech.wetech.flexmodel.sql.SqlSession;

import java.sql.Connection;

/**
 * @author cjbi
 */
public class SessionFactory {

  private final MappedModels mappedModels;
  private final ConnectionLifeCycleManager connectionLifeCycleManager;

  public SessionFactory(ConnectionLifeCycleManager connectionLifeCycleManager, MappedModels mappedModels) {
    this.connectionLifeCycleManager = connectionLifeCycleManager;
    this.mappedModels = new CachingMappedModels(mappedModels, connectionLifeCycleManager.getCache());

  }

  public Session openSession(String identifier) {
    Cache cache = connectionLifeCycleManager.getCache();
    return (Session) cache.retrieve(identifier, () -> createSession(identifier));
  }

  public Session createSession(String identifier) {
    return switch (connectionLifeCycleManager.getDataSourceProvider(identifier)) {
      case JdbcDataSourceProvider ignored -> {
        Connection connection = connectionLifeCycleManager.getSqlConnectionHolder().getOrCreateConnection(identifier);
        SqlContext sqlContext = new SqlContext(identifier, new NamedParameterSqlExecutor(connection), mappedModels);
        yield new SqlSession(sqlContext);
      }
      case MongoDataSourceProvider ignored -> {
        MongoDatabase mongoDatabase = connectionLifeCycleManager.getMongoDatabaseMap().get(identifier);
        MongoContext mongoContext = new MongoContext(identifier, mongoDatabase, mappedModels);
        yield new MongoSession(mongoContext);
      }
      case null,
        default -> throw new IllegalStateException("Unexpected identifier: " + identifier);
    };
  }

}
