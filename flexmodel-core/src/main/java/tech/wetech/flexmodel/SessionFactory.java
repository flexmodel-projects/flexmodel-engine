package tech.wetech.flexmodel;

import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.cache.Cache;
import tech.wetech.flexmodel.cache.CachingMappedModels;
import tech.wetech.flexmodel.cache.ConcurrentHashMapCache;
import tech.wetech.flexmodel.event.DomainEventPublisher;
import tech.wetech.flexmodel.mongodb.MongoContext;
import tech.wetech.flexmodel.mongodb.MongoDataSourceProvider;
import tech.wetech.flexmodel.mongodb.MongoSession;
import tech.wetech.flexmodel.sql.*;
import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author cjbi
 */
public class SessionFactory {

  private MappedModels mappedModels;
  private final Map<String, DataSourceProvider> dataSourceProviderMap = new HashMap<>();
  private final Cache cache;
  private final Logger log = LoggerFactory.getLogger(SessionFactory.class);
  private final JsonObjectConverter jsonObjectConverter;

  SessionFactory(String defaultIdentifier, DataSourceProvider defaultDataSourceProvider, Cache cache, String importScript) {
    this.cache = cache;
    this.jsonObjectConverter = new JacksonObjectConverter();
    addDataSourceProvider(defaultIdentifier, defaultDataSourceProvider);
    if (defaultDataSourceProvider instanceof JdbcDataSourceProvider jdbcDataSourceProvider) {
      this.mappedModels = new CachingMappedModels(new JdbcMappedModels(jdbcDataSourceProvider.dataSource(), jsonObjectConverter), cache);
    } else if (defaultDataSourceProvider instanceof MongoDataSourceProvider mongoDataSourceProvider) {
      this.mappedModels = new MapMappedModels();
    }
    loadScript(defaultIdentifier, importScript);
  }

  @SuppressWarnings("unchecked")
  public void loadScript(String schemaName, String scriptName) {
    try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(scriptName)) {
      if (is != null) {
        String scriptJSON = new String(is.readAllBytes());
        Map<String, Object> map = jsonObjectConverter.parseToMap(scriptJSON);
        List<Map<String, Object>> entityList = (List<Map<String, Object>>) map.get("schema");
        try (Session session = createFailSafeSession(schemaName)) {
          for (Map<String, Object> entityMap : entityList) {
            try {
              Entity newer = jsonObjectConverter.convertValue(entityMap, Entity.class);
              Entity older = (Entity) session.getModel(newer.getName());
              if (older == null) {
                session.createEntity(newer);
              } else {
                List<TypedField<?, ?>> fields = older.getFields();
                for (TypedField<?, ?> field : fields) {
                  if (older.getField(field.getName()) == null) {
                    session.createField(field);
                  } else {
                    session.modifyField(field);
                  }
                }
              }
            } catch (Exception e) {
              log.debug("Import script error: {}", e.getMessage(), e);
            }
          }
        }
      }
    } catch (IOException e) {
      log.debug("Read import script error: {}", e.getMessage(), e);
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
  }

  public <T> void subscribeEvent(Class<T> subscribedToEventType, Consumer<T> event) {
    DomainEventPublisher.instance().subscribe(subscribedToEventType, event);
  }

  /**
   * 宽松模式:
   * 允许ddl语句的错误
   *
   * @param identifier
   * @return
   */
  private Session createFailSafeSession(String identifier) {
    try {
      return switch (dataSourceProviderMap.get(identifier)) {
        case JdbcDataSourceProvider jdbc -> {
          Connection connection = jdbc.dataSource().getConnection();
          SqlContext sqlContext = new SqlContext(identifier, new NamedParameterSqlExecutor(connection), mappedModels, jsonObjectConverter);
          sqlContext.setFailSafe(true);
          yield new SqlSession(sqlContext);
        }
        case MongoDataSourceProvider mongodb -> {
          MongoDatabase mongoDatabase = mongodb.mongoDatabase();
          MongoContext mongoContext = new MongoContext(identifier, mongoDatabase, mappedModels, jsonObjectConverter);
          mongoContext.setFailSafe(true);
          yield new MongoSession(mongoContext);
        }
        case null,
          default -> throw new IllegalStateException("Unexpected identifier: " + identifier);
      };
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public Session createSession(String identifier) {
    try {
      return switch (dataSourceProviderMap.get(identifier)) {
        case JdbcDataSourceProvider jdbc -> {
          Connection connection = jdbc.dataSource().getConnection();
          SqlContext sqlContext = new SqlContext(identifier, new NamedParameterSqlExecutor(connection), mappedModels, jsonObjectConverter);
          yield new SqlSession(sqlContext);
        }
        case MongoDataSourceProvider mongodb -> {
          MongoDatabase mongoDatabase = mongodb.mongoDatabase();
          MongoContext mongoContext = new MongoContext(identifier, mongoDatabase, mappedModels, jsonObjectConverter);
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
    private String importScript = "import.json";

    Builder() {
    }

    public Builder setCache(Cache cache) {
      this.cache = cache;
      return this;
    }

    public Builder setImportScript(String importScript) {
      this.importScript = importScript;
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
      return new SessionFactory(defaultIdentifier, defaultDataSourceProvider, cache, importScript);
    }

  }

}
