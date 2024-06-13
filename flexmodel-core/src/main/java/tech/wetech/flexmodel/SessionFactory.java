package tech.wetech.flexmodel;

import com.mongodb.client.MongoDatabase;
import tech.wetech.flexmodel.cache.Cache;
import tech.wetech.flexmodel.cache.CachingMappedModels;
import tech.wetech.flexmodel.cache.ConcurrentHashMapCache;
import tech.wetech.flexmodel.event.DomainEventPublisher;
import tech.wetech.flexmodel.mongodb.MongoContext;
import tech.wetech.flexmodel.mongodb.MongoDataSourceProvider;
import tech.wetech.flexmodel.mongodb.MongoSession;
import tech.wetech.flexmodel.sql.*;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author cjbi
 */
public class SessionFactory {

  private MappedModels mappedModels;
  private final Map<String, DataSourceProvider> dataSourceProviderMap = new HashMap<>();
  private final Cache cache;

  SessionFactory(String defaultIdentifier, DataSourceProvider defaultDataSourceProvider, Cache cache) {
    this.cache = cache;
    addDataSourceProvider(defaultIdentifier, defaultDataSourceProvider);
    if (defaultDataSourceProvider instanceof JdbcDataSourceProvider jdbcDataSourceProvider) {
      this.mappedModels = new CachingMappedModels(new JdbcMappedModels(jdbcDataSourceProvider.dataSource()), cache);
    } else if (defaultDataSourceProvider instanceof MongoDataSourceProvider mongoDataSourceProvider) {
      this.mappedModels = new MapMappedModels();
    }
  }

  public Map<String, DataSourceProvider> getDataSourceProviderMap() {
    return dataSourceProviderMap;
  }

  public Cache getCache() {
    return cache;
  }

  public static Builder builder() {
    return new Builder();
  }

  public void addDataSourceProvider(String identifier, DataSourceProvider dataSource) {
    dataSourceProviderMap.put(identifier, dataSource);
  }

  public DataSourceProvider getDataSourceProvider(String identifier) {
    return dataSourceProviderMap.get(identifier);
  }

  public void removeDataSourceProvider(String identifier) {
    dataSourceProviderMap.remove(identifier);
    mappedModels.removeAll(identifier);
  }

  public <T> void subscribeEvent(Class<T> subscribedToEventType, Consumer<T> event) {
    DomainEventPublisher.instance().subscribe(subscribedToEventType, event);
  }

  public Session createSession(String identifier) {
    try {
      return switch (dataSourceProviderMap.get(identifier)) {
        case JdbcDataSourceProvider jdbc -> {
          Connection connection = jdbc.dataSource().getConnection();
          SqlContext sqlContext = new SqlContext(identifier, new NamedParameterSqlExecutor(connection), mappedModels);
          yield new SqlSession(sqlContext);
        }
        case MongoDataSourceProvider mongodb -> {
          MongoDatabase mongoDatabase = mongodb.mongoDatabase();
          MongoContext mongoContext = new MongoContext(identifier, mongoDatabase, mappedModels);
          yield new MongoSession(mongoContext);
        }
        case null,
          default -> throw new IllegalStateException("Unexpected identifier: " + identifier);
      };
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static class Builder {
    private Cache cache;
    private String defaultIdentifier = "default";
    private DataSourceProvider defaultDataSourceProvider = null;

    Builder() {
    }

    public Builder setCache(Cache cache) {
      this.cache = cache;
      return this;
    }

    public Builder setDefaultDataSourceProvider(String defaultIdentifier, DataSourceProvider dataSourceProvider) {
      this.defaultIdentifier = defaultIdentifier;
      this.defaultDataSourceProvider = dataSourceProvider;
      return this;
    }

    public Builder setDefaultDataSourceProvider(DataSourceProvider defaultDataSourceProvider) {
      this.defaultDataSourceProvider = defaultDataSourceProvider;
      return this;
    }

    public SessionFactory build() {
      if (defaultDataSourceProvider == null) {
        throw new IllegalStateException("Please set defaultDataSourceProvider");
      }
      if (cache == null) {
        this.cache = new ConcurrentHashMapCache();
      }
      return new SessionFactory(defaultIdentifier, defaultDataSourceProvider, cache);
    }

  }

}
