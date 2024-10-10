package tech.wetech.flexmodel;

import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.cache.Cache;
import tech.wetech.flexmodel.cache.CachingMappedModels;
import tech.wetech.flexmodel.cache.ConcurrentHashMapCache;
import tech.wetech.flexmodel.mongodb.MongoContext;
import tech.wetech.flexmodel.mongodb.MongoDataSourceProvider;
import tech.wetech.flexmodel.mongodb.MongoSession;
import tech.wetech.flexmodel.sql.*;
import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author cjbi
 */
public class SessionFactory {

  private final MappedModels mappedModels;
  private final Map<String, DataSourceProvider> dataSourceProviders = new HashMap<>();
  private final Cache cache;
  private final Logger log = LoggerFactory.getLogger(SessionFactory.class);
  private final JsonObjectConverter jsonObjectConverter;

  private final Map<Class, Consumer> globalSubscribers = new HashMap<>();

  SessionFactory(String defaultIdentifier, DataSourceProvider defaultDataSourceProvider, Cache cache, String importScript) {
    this.cache = cache;
    this.jsonObjectConverter = new JacksonObjectConverter();
    addDataSourceProvider(defaultIdentifier, defaultDataSourceProvider);
    this.mappedModels = initializeMappedModels(defaultDataSourceProvider);
    processBuildItem();
    loadScript(defaultIdentifier, importScript);
  }

  private MappedModels initializeMappedModels(DataSourceProvider dataSourceProvider) {
    if (dataSourceProvider instanceof JdbcDataSourceProvider jdbcDataSourceProvider) {
      return new CachingMappedModels(new JdbcMappedModels(jdbcDataSourceProvider.dataSource(), jsonObjectConverter), cache);
    } else if (dataSourceProvider instanceof MongoDataSourceProvider) {
      return new MapMappedModels();
    } else {
      throw new IllegalArgumentException("Unsupported DataSourceProvider");
    }
  }

  /**
   * 处理构建步骤的项目
   */
  void processBuildItem() {
    // 处理编译器处理的过的模型
    ServiceLoader.load(BuildItem.class).forEach(this::processBuildItem);
  }

  private void processBuildItem(BuildItem buildItem) {
    List<Model> allModels = buildItem.getModels();
    allModels.forEach(model -> cache.put(buildItem.getSchemaName() + ":" + model.getName(), model));
  }

  public void loadScript(String schemaName, String scriptName) {
    try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(scriptName)) {
      if (is == null) {
        log.warn("Script file not found: {}", scriptName);
        return;
      }
      String scriptJSON = new String(is.readAllBytes());
      ImportDescribe describe = jsonObjectConverter.parseToObject(scriptJSON, ImportDescribe.class);

      try (Session session = createFailSafeSession(schemaName)) {
        List<RelationField> lazyCreateList = processModels(describe.getSchema(), session);
        lazyCreateRelationFields(lazyCreateList, session);
        processImportData(describe.getData(), session);
      }
    } catch (IOException e) {
      log.error("Failed to read import script: {}", e.getMessage(), e);
    }
  }

  private List<RelationField> processModels(List<Model> models, Session session) {
    List<RelationField> lazyCreateList = new ArrayList<>();
    for (Model model : models) {
      if (!(model instanceof Entity newer)) {
        log.warn("Unsupported model type: {}", model.getName());
        continue;
      }

      try {
        Entity older = (Entity) session.getModel(newer.getName());
        updateEntity(session, newer, older, lazyCreateList);
      } catch (Exception e) {
        log.warn("Error processing model: {}", e.getMessage(), e);
      }
    }
    return lazyCreateList;
  }

  private void updateEntity(Session session, Entity newer, Entity older, List<RelationField> lazyCreateList) throws Exception {
    Entity clonedNewer = newer.clone();
    clonedNewer.getFields().stream()
      .filter(field -> field instanceof RelationField)
      .forEach(field -> {
        clonedNewer.removeField(field.getName());
        lazyCreateList.add((RelationField) field);
      });

    try {
      session.createEntity(clonedNewer);
    } catch (Exception e) {
      updateEntityFields(session, older);
    }
  }

  private void updateEntityFields(Session session, Entity older) {
    older.getFields().forEach(field -> {
      try {
        if (older.getField(field.getName()) == null) {
          session.createField(field);
        } else if (!field.equals(older.getField(field.getName()))) {
          session.modifyField(field);
        }
      } catch (Exception e) {
        log.warn("Error updating field: {}", e.getMessage(), e);
      }
    });
  }

  private void lazyCreateRelationFields(List<RelationField> relationFields, Session session) {
    for (RelationField relationField : relationFields) {
      try {
        session.createField(relationField);
      } catch (Exception e) {
        log.warn("Error lazy create relation field: {}, message: {}", relationField.getName(), e.getMessage());
      }
    }
  }

  private void processImportData(List<ImportDescribe.ImportData> data, Session session) {
    data.forEach(item -> {
      try {
        session.insertAll(item.getModelName(), item.getValues());
      } catch (Exception e) {
        log.warn("Error importing data: {}", e.getMessage(), e);
      }
    });
  }

  public Set<String> getSchemaNames() {
    return dataSourceProviders.keySet();
  }

  public List<Model> getModels(String schemaName) {
    return mappedModels.lookup(schemaName);
  }

  public Cache getCache() {
    return cache;
  }

  public static Builder builder() {
    return new Builder();
  }

  public void addDataSourceProvider(String identifier, DataSourceProvider dataSource) {
    dataSourceProviders.put(identifier, dataSource);
  }

  public DataSourceProvider getDataSourceProvider(String identifier) {
    return dataSourceProviders.get(identifier);
  }

  public void removeDataSourceProvider(String identifier) {
    dataSourceProviders.remove(identifier);
  }

  public <T> void subscribeEvent(Class<T> subscribedToEventType, Consumer<T> event) {
    globalSubscribers.put(subscribedToEventType, event);
  }

  public Map<Class, Consumer> getGlobalSubscribers() {
    return globalSubscribers;
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
      return switch (dataSourceProviders.get(identifier)) {
        case JdbcDataSourceProvider jdbc -> {
          Connection connection = jdbc.dataSource().getConnection();
          SqlContext sqlContext = new SqlContext(identifier, new NamedParameterSqlExecutor(connection), mappedModels, jsonObjectConverter, this);
          sqlContext.setFailFast(false);
          yield new SqlSession(sqlContext);
        }
        case MongoDataSourceProvider mongodb -> {
          MongoDatabase mongoDatabase = mongodb.mongoDatabase();
          MongoContext mongoContext = new MongoContext(identifier, mongoDatabase, mappedModels, jsonObjectConverter, this);
          mongoContext.setFailFast(false);
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
      return switch (dataSourceProviders.get(identifier)) {
        case JdbcDataSourceProvider jdbc -> {
          Connection connection = jdbc.dataSource().getConnection();
          SqlContext sqlContext = new SqlContext(identifier, new NamedParameterSqlExecutor(connection), mappedModels, jsonObjectConverter, this);
          yield new SqlSession(sqlContext);
        }
        case MongoDataSourceProvider mongodb -> {
          MongoDatabase mongoDatabase = mongodb.mongoDatabase();
          MongoContext mongoContext = new MongoContext(identifier, mongoDatabase, mappedModels, jsonObjectConverter, this);
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

  public JsonObjectConverter getJsonObjectConverter() {
    return jsonObjectConverter;
  }
}
