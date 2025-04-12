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
import tech.wetech.flexmodel.parser.ASTNodeConverter;
import tech.wetech.flexmodel.parser.impl.ModelParser;
import tech.wetech.flexmodel.parser.impl.ParseException;
import tech.wetech.flexmodel.sql.*;
import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author cjbi
 */
public class SessionFactory {

  private final MappedModels mappedModels;
  private final Map<String, DataSourceProvider> dataSourceProviders = new HashMap<>();
  private final Cache cache;
  private final Logger log = LoggerFactory.getLogger(SessionFactory.class);
  private final JsonObjectConverter jsonObjectConverter;
  private final boolean failsafe;

  SessionFactory(DataSourceProvider defaultDataSourceProvider, List<DataSourceProvider> dataSourceProviders, Cache cache, boolean failsafe) {
    this.cache = cache;
    this.jsonObjectConverter = new JacksonObjectConverter();
    addDataSourceProvider(defaultDataSourceProvider);
    dataSourceProviders.forEach(this::addDataSourceProvider);
    this.mappedModels = initializeMappedModels(defaultDataSourceProvider);
    this.failsafe = failsafe;
    processBuildItem();
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
    buildItem.getSchema().forEach(model -> cache.put(buildItem.getSchemaName() + ":" + model.getName(), model));
    try (Session session = createFailsafeSession(buildItem.getSchemaName())) {
      processModels(buildItem.getSchema(), session);
      processImportData(buildItem.getData(), session);
    }
  }

  public void loadIDLString(String schemaName, String idlString) throws ParseException {
    ModelParser parser = new ModelParser(new StringReader(idlString));
    List<ModelParser.ASTNode> ast = parser.CompilationUnit();
    List<SchemaObject> schema = new ArrayList<>();
    for (ModelParser.ASTNode obj : ast) {
      schema.add(ASTNodeConverter.toSchemaObject(obj));
    }
    try (Session session = createFailsafeSession(schemaName)) {
      processModels(schema, session);
    }
  }

  public void loadJSONString(String schemaName, String jsonString) {
    ImportDescribe describe = jsonObjectConverter.parseToObject(jsonString, ImportDescribe.class);
    try (Session session = createFailsafeSession(schemaName)) {
      processModels(describe.getSchema(), session);
      processImportData(describe.getData(), session);
    }
  }

  public void loadScript(String schemaName, String scriptName, ClassLoader classLoader) {
    try (InputStream is = classLoader.getResourceAsStream(scriptName)) {
      if (is == null) {
        log.warn("Script file not found: {}", scriptName);
        throw new RuntimeException("Script file not found: " + scriptName);
      }
      String scriptString = new String(is.readAllBytes());
      if (scriptName.endsWith(".json")) {
        loadJSONString(schemaName, scriptString);
      } else if (scriptName.endsWith(".idl")) {
        loadIDLString(schemaName, scriptString);
      } else {
        // unsupported script type
        log.warn("Unsupported script type: {}, must be .json or .idl", scriptName);
      }
    } catch (IOException e) {
      log.error("Failed to read import script: {}", e.getMessage(), e);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  public void loadScript(String schemaName, String scriptName) {
    loadScript(schemaName, scriptName, this.getClass().getClassLoader());
  }

  private void processModels(List<SchemaObject> models, Session session) {
    Map<String, SchemaObject> wrapperMap = session.getAllModels().stream().collect(Collectors.toMap(SchemaObject::getName, m -> m));
    for (SchemaObject model : models) {
      SchemaObject older = wrapperMap.get(model.getName());
      if (older != null && Objects.equals(older.getIdl(), model.getIdl())) {
        continue;
      }
      if (model instanceof Entity newer) {
        try {
          updateEntity(session, newer, (Entity) older);
        } catch (Exception e) {
          log.warn("Error processing model: {}", e.getMessage(), e);
        }
      } else if (model instanceof Enum newer) {
        try {
          updateEnum(session, newer);
        } catch (Exception e) {
          log.warn("Error processing model: {}", e.getMessage(), e);
        }
      }
    }
  }

  private void updateEnum(Session session, Enum newer) {
    try {
      session.dropModel(newer.getName());
      session.createEnum(newer);
    } catch (Exception e) {
      log.warn("Error processing model: {}", e.getMessage(), e);
    }
  }

  private void updateEntity(Session session, Entity newer, Entity older) throws Exception {
    try {
      session.createEntity(newer.clone());
    } catch (Exception e) {
      if (older != null) {
        updateEntityFields(session, newer, older);
      }
    }
  }

  private void updateEntityFields(Session session, Entity newer, Entity older) {
    newer.getFields().forEach(field -> {
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

  private void processImportData(List<ImportDescribe.ImportData> data, Session session) {
    data.forEach(item -> {
      try {
        session.insertAll(item.getModelName(), item.getValues());
      } catch (Exception e) {
        log.warn("Error importing data: {}", e.getMessage());
      }
    });
  }

  public Set<String> getSchemaNames() {
    return dataSourceProviders.keySet();
  }

  public List<SchemaObject> getModels(String schemaName) {
    return mappedModels.lookup(schemaName);
  }

  public Cache getCache() {
    return cache;
  }

  public static Builder builder() {
    return new Builder();
  }

  public void addDataSourceProvider(DataSourceProvider dataSource) {
    dataSourceProviders.put(dataSource.getId(), dataSource);
  }

  public DataSourceProvider getDataSourceProvider(String dsId) {
    return dataSourceProviders.get(dsId);
  }

  public void removeDataSourceProvider(String dsId) {
    dataSourceProviders.remove(dsId);
  }

  /**
   * 宽松模式:
   * 允许ddl语句的错误
   *
   * @param id
   * @return
   */
  public Session createFailsafeSession(String id) {
    try {
      return switch (dataSourceProviders.get(id)) {
        case JdbcDataSourceProvider jdbc -> {
          Connection connection = jdbc.dataSource().getConnection();
          SqlContext sqlContext = new SqlContext(id, new NamedParameterSqlExecutor(connection), mappedModels, jsonObjectConverter, this);
          sqlContext.setFailsafe(true);
          yield new SqlSession(sqlContext);
        }
        case MongoDataSourceProvider mongodb -> {
          MongoDatabase mongoDatabase = mongodb.mongoDatabase();
          MongoContext mongoContext = new MongoContext(id, mongoDatabase, mappedModels, jsonObjectConverter, this);
          mongoContext.setFailsafe(true);
          yield new MongoSession(mongoContext);
        }
        case null,
             default -> throw new IllegalStateException("Unexpected identifier: " + id);
      };
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public Session createSession(String identifier) {
    try {
      if (failsafe) {
        return createFailsafeSession(identifier);
      }
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
    private DataSourceProvider defaultDataSourceProvider = null;
    private final List<DataSourceProvider> dataSourceProviders = new ArrayList<>();
    private boolean failsafe = false;

    Builder() {
    }

    public Builder setCache(Cache cache) {
      this.cache = cache;
      return this;
    }

    public Builder setDefaultDataSourceProvider(DataSourceProvider dataSourceProvider) {
      this.defaultDataSourceProvider = dataSourceProvider;
      return this;
    }

    public Builder addDataSourceProvider(DataSourceProvider dataSourceProvider) {
      this.dataSourceProviders.add(dataSourceProvider);
      return this;
    }

    public Builder setFailsafe(boolean failsafe) {
      this.failsafe = failsafe;
      return this;
    }

    public SessionFactory build() {
      if (defaultDataSourceProvider == null) {
        throw new IllegalStateException("Please set defaultDataSourceProvider");
      }
      if (cache == null) {
        this.cache = new ConcurrentHashMapCache();
      }
      return new SessionFactory(defaultDataSourceProvider, dataSourceProviders, cache, failsafe);
    }
  }

  public JsonObjectConverter getJsonObjectConverter() {
    return jsonObjectConverter;
  }
}
