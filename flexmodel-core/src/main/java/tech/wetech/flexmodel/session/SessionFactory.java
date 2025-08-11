package tech.wetech.flexmodel.session;

import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.DataSourceProvider;
import tech.wetech.flexmodel.ImportDescribe;
import tech.wetech.flexmodel.JsonObjectConverter;
import tech.wetech.flexmodel.ModelRegistry;
import tech.wetech.flexmodel.cache.Cache;
import tech.wetech.flexmodel.cache.CachingModelRegistry;
import tech.wetech.flexmodel.cache.ConcurrentHashMapCache;
import tech.wetech.flexmodel.cache.InMemoryModelRegistry;
import tech.wetech.flexmodel.model.EntityDefinition;
import tech.wetech.flexmodel.model.EnumDefinition;
import tech.wetech.flexmodel.model.SchemaObject;
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
import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author cjbi
 */
public class SessionFactory {

  private final ModelRegistry modelRepository;
  private final DataSourceProvider defaultDataSourceProvider;
  private final Map<String, DataSourceProvider> dataSourceProviders = new HashMap<>();
  private final Cache cache;
  private final Logger log = LoggerFactory.getLogger(SessionFactory.class);
  private final JsonObjectConverter jsonObjectConverter;
  private final boolean failsafe;
  private final MemoryScriptManager memoryScriptManager;

  SessionFactory(DataSourceProvider defaultDataSourceProvider, List<DataSourceProvider> dataSourceProviders, Cache cache, boolean failsafe) {
    this.cache = cache;
    this.jsonObjectConverter = new JacksonObjectConverter();
    this.memoryScriptManager = new MemoryScriptManager();
    this.defaultDataSourceProvider = defaultDataSourceProvider;
    addDataSourceProvider(defaultDataSourceProvider);
    dataSourceProviders.forEach(this::addDataSourceProvider);
    this.modelRepository = initializeModelRepository(defaultDataSourceProvider);
    this.failsafe = failsafe;
    processBuildItem();
  }

  private ModelRegistry initializeModelRepository(DataSourceProvider dataSourceProvider) {
    if (dataSourceProvider instanceof JdbcDataSourceProvider jdbcDataSourceProvider) {
      return new CachingModelRegistry(new JdbcModelRegistry(jdbcDataSourceProvider.dataSource(), jsonObjectConverter), cache);
    } else if (dataSourceProvider instanceof MongoDataSourceProvider) {
      return new InMemoryModelRegistry();
    } else {
      throw new IllegalArgumentException("Unsupported DataSourceProvider");
    }
  }

  /**
   * 处理构建步骤的项目
   */
  void processBuildItem() {
    // 将BuildItem脚本加载到内存中
    memoryScriptManager.loadScriptsFromBuildItems();

    // 将内存中的脚本应用到缓存中
    memoryScriptManager.getSchemaNames().forEach(schemaName -> {
      MemoryScriptManager.SchemaScriptConfig config = memoryScriptManager.getScriptConfig(schemaName);
      config.getSchema().forEach(model -> cache.put(schemaName + ":" + model.getName(), model));
      try (Session session = createFailsafeSession(schemaName)) {
        processModels(config.getSchema(), session);
        processImportData(config.getData(), session);
      }
    });
  }

  public void loadIDLString(String schemaName, String idlString) {
    try {
      // 处理空字符串或只包含空白字符的字符串
      if (idlString == null || idlString.trim().isEmpty()) {
        log.info("Empty or null IDL string provided for schema: {}", schemaName);
        return;
      }

      // 创建IDL解析器
      ModelParser parser =
        new ModelParser(new java.io.StringReader(idlString));

      // 解析IDL字符串，获取AST节点列表
      List<ModelParser.ASTNode> ast = parser.CompilationUnit();

      // 将AST节点转换为SchemaObject列表
      List<SchemaObject> schema = new ArrayList<>();
      for (ModelParser.ASTNode obj : ast) {
        SchemaObject schemaObject = ASTNodeConverter.toSchemaObject(obj);
        if (schemaObject != null) {
          schema.add(schemaObject);
        } else {
          log.warn("Failed to convert AST node to SchemaObject: {}", obj);
        }
      }

      // 使用故障安全会话处理模型 - 使用默认数据源
      try (Session session = createFailsafeSession(dataSourceProviders.keySet().iterator().next())) {
        processModels(schema, session);
      }

      log.info("Successfully loaded {} models from IDL for schema: {}", schema.size(), schemaName);
    } catch (ParseException e) {
      log.error("Failed to parse IDL string for schema {}: {}", schemaName, e.getMessage(), e);
      throw new RuntimeException("IDL parsing failed: " + e.getMessage(), e);
    } catch (Exception e) {
      log.error("Failed to load IDL string for schema {}: {}", schemaName, e.getMessage(), e);
      throw new RuntimeException("Failed to load IDL: " + e.getMessage(), e);
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
    }
  }

  public void loadScript(String schemaName, String scriptName) {
    loadScript(schemaName, scriptName, this.getClass().getClassLoader());
  }

  private void processModels(List<SchemaObject> models, Session session) {
    Map<String, SchemaObject> wrapperMap = session.schema().getAllModels().stream().collect(Collectors.toMap(SchemaObject::getName, m -> m));
    for (SchemaObject model : models) {
      SchemaObject older = wrapperMap.get(model.getName());
      if (older != null && Objects.equals(older.getIdl(), model.getIdl())) {
        continue;
      }
      if (model instanceof EntityDefinition newer) {
        try {
          updateEntity(session, newer, (EntityDefinition) older);
        } catch (Exception e) {
          log.warn("Error processing model: {}", e.getMessage(), e);
        }
      } else if (model instanceof EnumDefinition newer) {
        try {
          updateEnum(session, newer);
        } catch (Exception e) {
          log.warn("Error processing model: {}", e.getMessage(), e);
        }
      }
    }
  }

  private void updateEnum(Session session, EnumDefinition newer) {
    try {
      session.schema().dropModel(newer.getName());
      session.schema().createEnum(newer);
    } catch (Exception e) {
      log.warn("Error processing model: {}", e.getMessage(), e);
    }
  }

  private void updateEntity(Session session, EntityDefinition newer, EntityDefinition older) throws Exception {
    try {
      session.schema().createEntity(newer.clone());
    } catch (Exception e) {
      if (older != null) {
        updateEntityFields(session, newer, older);
      }
    }
  }

  private void updateEntityFields(Session session, EntityDefinition newer, EntityDefinition older) {
    newer.getFields().forEach(field -> {
      try {
        if (older.getField(field.getName()) == null) {
          session.schema().createField(field);
        } else if (!field.equals(older.getField(field.getName()))) {
          session.schema().modifyField(field);
        }
      } catch (Exception e) {
        log.warn("Error updating field: {}", e.getMessage(), e);
      }
    });
  }

  private void processImportData(List<ImportDescribe.ImportData> data, Session session) {
    data.forEach(item -> {
      try {
        session.data().insertAll(item.getModelName(), item.getValues());
      } catch (Exception e) {
        log.warn("Error importing data: {}", e.getMessage());
      }
    });
  }

  public Set<String> getSchemaNames() {
    return dataSourceProviders.keySet();
  }

  public List<SchemaObject> getModels(String schemaName) {
    return modelRepository.getAllRegistered(schemaName);
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

  public Session createSession() {
    return createSession(defaultDataSourceProvider.getId());
  }

  public Session createFailsefeSession() {
    return createFailsafeSession(defaultDataSourceProvider.getId());
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
          SqlContext sqlContext = new SqlContext(id, new NamedParameterSqlExecutor(connection), modelRepository, jsonObjectConverter, this);
          sqlContext.setFailsafe(true);
          yield new SqlSession(sqlContext);
        }
        case MongoDataSourceProvider mongodb -> {
          MongoDatabase mongoDatabase = mongodb.mongoDatabase();
          MongoContext mongoContext = new MongoContext(id, mongoDatabase, modelRepository, jsonObjectConverter, this);
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
          SqlContext sqlContext = new SqlContext(identifier, new NamedParameterSqlExecutor(connection), modelRepository, jsonObjectConverter, this);
          yield new SqlSession(sqlContext);
        }
        case MongoDataSourceProvider mongodb -> {
          MongoDatabase mongoDatabase = mongodb.mongoDatabase();
          MongoContext mongoContext = new MongoContext(identifier, mongoDatabase, modelRepository, jsonObjectConverter, this);
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

  /**
   * 获取内存脚本管理器
   */
  public MemoryScriptManager getMemoryScriptManager() {
    return memoryScriptManager;
  }

  public String getDefaultSchema() {
    return defaultDataSourceProvider.getId();
  }
}
