package tech.wetech.flexmodel;

import com.mongodb.client.MongoDatabase;
import tech.wetech.flexmodel.cache.Cache;
import tech.wetech.flexmodel.cache.CachingMappedModels;
import tech.wetech.flexmodel.cache.ConcurrentHashMapCache;
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
  private final Cache cache;

  public SessionFactory(ConnectionLifeCycleManager connectionLifeCycleManager, MappedModels mappedModels, Cache cache) {
    this.connectionLifeCycleManager = connectionLifeCycleManager;
    this.mappedModels = mappedModels;
    this.cache = cache;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Session openSession(String identifier) {
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

  public static class Builder {
    private MappedModels mappedModels;
    private ConnectionLifeCycleManager connectionLifeCycleManager;
    private Cache cache;

    Builder() {
    }

    public Builder setMappedModels(MappedModels mappedModels) {
      this.mappedModels = mappedModels;
      return this;
    }

    public Builder setConnectionLifeCycleManager(ConnectionLifeCycleManager connectionLifeCycleManager) {
      this.connectionLifeCycleManager = connectionLifeCycleManager;
      return this;
    }

    public Builder setCache(Cache cache) {
      this.cache = cache;
      return this;
    }

    public SessionFactory build() {
      if (connectionLifeCycleManager == null) {
        throw new IllegalStateException("Please set connectionLifeCycleManager");
      }
      if (mappedModels == null) {
        throw new IllegalStateException("Please set mappedModels");
      }
      if (cache == null) {
        this.cache = new ConcurrentHashMapCache();
      }
      connectionLifeCycleManager.setCache(cache);
      return new SessionFactory(connectionLifeCycleManager, new CachingMappedModels(mappedModels, cache), cache);
    }

  }

}
