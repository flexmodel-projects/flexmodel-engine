# FlexModel API 文档

## 概述

FlexModel API 提供了统一的数据访问层接口，支持多种数据库和动态数据建模。本文档详细介绍了核心API的使用方法。

## 核心API

### SessionFactory

`SessionFactory` 是FlexModel的核心入口，负责创建和管理数据访问会话。

#### 创建SessionFactory

```java
// 基本创建
SessionFactory sessionFactory = SessionFactory.builder()
    .setDefaultDataSourceProvider(new JdbcDataSourceProvider(dataSource))
    .build();

// 完整配置
SessionFactory sessionFactory = SessionFactory.builder()
    .setDefaultDataSourceProvider(new JdbcDataSourceProvider(dataSource))
    .setCache(new ConcurrentHashMapCache())
    .setFailsafe(true)
    .addDataSourceProvider(new MongoDataSourceProvider(mongoDatabase))
    .build();
```

#### 配置选项

| 配置项 | 类型 | 说明 | 默认值 |
|--------|------|------|--------|
| defaultDataSourceProvider | DataSourceProvider | 默认数据源提供者 | 必需 |
| cache | Cache | 缓存实现 | ConcurrentHashMapCache |
| failsafe | boolean | 是否启用故障安全模式 | false |
| dataSourceProviders | List<DataSourceProvider> | 额外数据源提供者 | 空列表 |

### Session

`Session` 代表一个数据访问会话，提供数据操作和模型管理功能。

#### 创建Session

```java
// 创建会话
try (Session session = sessionFactory.createSession("mySchema")) {
    // 使用会话进行数据操作
    DataOperations operations = session.getDataOperations();
    List<Map<String, Object>> results = operations.query("SELECT * FROM users");
}
```

#### 故障安全模式

```java
// 创建故障安全会话
try (Session session = sessionFactory.createFailsafeSession("mySchema")) {
    // 即使出现异常也不会影响其他操作
    session.getDataOperations().query("SELECT * FROM users");
}
```

### DataOperations

`DataOperations` 提供核心的数据操作功能。

#### 查询操作

```java
DataOperations operations = session.getDataOperations();

// 基本查询
List<Map<String, Object>> users = operations.query("SELECT * FROM users");

// 参数化查询
List<Map<String, Object>> users = operations.query(
    "SELECT * FROM users WHERE age > ? AND status = ?",
    Arrays.asList(18, "active")
);

// 分页查询
Query query = new Query("SELECT * FROM users")
    .setPage(1)
    .setSize(10)
    .addOrderBy("id", Direction.DESC);

List<Map<String, Object>> users = operations.query(query);
```

#### 插入操作

```java
// 插入单条记录
Map<String, Object> user = new HashMap<>();
user.put("name", "John Doe");
user.put("email", "john@example.com");
user.put("age", 30);

operations.insert("users", user);

// 批量插入
List<Map<String, Object>> users = Arrays.asList(user1, user2, user3);
operations.batchInsert("users", users);
```

#### 更新操作

```java
// 更新记录
Map<String, Object> updates = new HashMap<>();
updates.put("status", "inactive");

operations.update("users", updates, "id = ?", Arrays.asList(1L));

// 批量更新
operations.batchUpdate("users", updates, "status = ?", Arrays.asList("active"));
```

#### 删除操作

```java
// 删除记录
operations.delete("users", "id = ?", Arrays.asList(1L));

// 批量删除
operations.batchDelete("users", "status = ?", Arrays.asList("inactive"));
```

### SchemaOperations

`SchemaOperations` 提供数据模型管理功能。

#### 模型同步

```java
SchemaOperations schemaOps = session.getSchemaOperations();

// 同步所有模型
List<SchemaObject> models = schemaOps.syncModels();

// 同步指定模型
Set<String> modelNames = Set.of("User", "Order");
List<SchemaObject> models = schemaOps.syncModels(modelNames);
```

#### 模型操作

```java
// 获取所有模型
List<SchemaObject> allModels = schemaOps.getAllModels();

// 获取指定模型
SchemaObject userModel = schemaOps.getModel("User");

// 删除模型
schemaOps.dropModel("User");
```

#### 实体操作

```java
// 创建实体
Entity userEntity = new Entity("User")
    .addField(new IntField("id").setIdentity(true).setAutoIncrement(true))
    .addField(new StringField("name").setLength(100).setNullable(false))
    .addField(new StringField("email").setLength(255).setUnique(true));

schemaOps.createEntity(userEntity);

// 创建字段
StringField phoneField = new StringField("phone").setLength(20);
schemaOps.createField(phoneField);

// 修改字段
phoneField.setLength(15);
schemaOps.modifyField(phoneField);

// 删除字段
schemaOps.dropField("User", "phone");
```

#### 索引操作

```java
// 创建索引
Index nameIndex = new Index("User")
    .addField("name", Direction.ASC)
    .setUnique(true);

schemaOps.createIndex(nameIndex);

// 删除索引
schemaOps.dropIndex("User", "name_index");
```

### 数据源提供者

#### JDBC数据源

```java
// 创建JDBC数据源
DataSource dataSource = new HikariDataSource();
((HikariDataSource) dataSource).setJdbcUrl("jdbc:mysql://localhost:3306/test");
((HikariDataSource) dataSource).setUsername("root");
((HikariDataSource) dataSource).setPassword("password");

JdbcDataSourceProvider jdbcProvider = new JdbcDataSourceProvider(dataSource);
```

#### MongoDB数据源

```java
// 创建MongoDB数据源
MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
MongoDatabase database = mongoClient.getDatabase("test");

MongoDataSourceProvider mongoProvider = new MongoDataSourceProvider(database);
```

### 缓存

#### 缓存接口

```java
public interface Cache {
    void put(String key, Object value);
    Object get(String key);
    void remove(String key);
    void clear();
    boolean containsKey(String key);
}
```

#### 内置缓存实现

```java
// 并发HashMap缓存
Cache cache = new ConcurrentHashMapCache();

// 自定义缓存实现
public class CustomCache implements Cache {
    private final Map<String, Object> cache = new HashMap<>();
    private final Object lock = new Object();
    
    @Override
    public void put(String key, Object value) {
        synchronized (lock) {
            cache.put(key, value);
        }
    }
    
    @Override
    public Object get(String key) {
        synchronized (lock) {
            return cache.get(key);
        }
    }
    
    // 实现其他方法...
}
```

### 查询构建器

#### Query类

```java
// 基本查询
Query query = new Query("SELECT * FROM users");

// 条件查询
Query query = new Query("SELECT * FROM users")
    .addWhere("age > ?", 18)
    .addWhere("status = ?", "active");

// 排序
Query query = new Query("SELECT * FROM users")
    .addOrderBy("name", Direction.ASC)
    .addOrderBy("age", Direction.DESC);

// 分页
Query query = new Query("SELECT * FROM users")
    .setPage(1)
    .setSize(10);

// 分组
Query query = new Query("SELECT department, COUNT(*) as count FROM users")
    .addGroupBy("department");

// 聚合
Query query = new Query("SELECT department, AVG(salary) as avg_salary FROM users")
    .addGroupBy("department")
    .addHaving("AVG(salary) > ?", 5000);
```

### 字段类型

#### 基本字段类型

```java
// 整数字段
IntField idField = new IntField("id")
    .setIdentity(true)
    .setAutoIncrement(true);

// 字符串字段
StringField nameField = new StringField("name")
    .setLength(100)
    .setNullable(false)
    .setDefaultValue("anonymous");

// 浮点数字段
FloatField priceField = new FloatField("price")
    .setPrecision(10)
    .setScale(2)
    .setDefaultValue(0.0);

// 布尔字段
BooleanField activeField = new BooleanField("active")
    .setDefaultValue(true);

// 日期时间字段
DateTimeField createdAtField = new DateTimeField("created_at")
    .setDefaultValue(GeneratedValue.CURRENT_TIMESTAMP);

// JSON字段
JSONField metadataField = new JSONField("metadata")
    .setDefaultValue("{}");

// 枚举字段
EnumField statusField = new EnumField("status")
    .addValue("ACTIVE")
    .addValue("INACTIVE")
    .addValue("PENDING")
    .setDefaultValue("PENDING");
```

#### 关系字段

```java
// 一对一关系
RelationField profileField = new RelationField("profile")
    .setType(RelationField.Type.ONE_TO_ONE)
    .setFrom("User")
    .setTo("UserProfile")
    .setForeignField("user_id");

// 一对多关系
RelationField ordersField = new RelationField("orders")
    .setType(RelationField.Type.ONE_TO_MANY)
    .setFrom("User")
    .setTo("Order")
    .setForeignField("user_id");

// 多对多关系
RelationField rolesField = new RelationField("roles")
    .setType(RelationField.Type.MANY_TO_MANY)
    .setFrom("User")
    .setTo("Role")
    .setJoinTable("user_roles")
    .setJoinColumn("user_id")
    .setInverseJoinColumn("role_id");
```

### 异常处理

#### 自定义异常

```java
// 数据访问异常
public class FlexModelException extends RuntimeException {
    public FlexModelException(String message) {
        super(message);
    }
    
    public FlexModelException(String message, Throwable cause) {
        super(message, cause);
    }
}

// 模型不存在异常
public class ModelNotFoundException extends FlexModelException {
    public ModelNotFoundException(String modelName) {
        super("Model not found: " + modelName);
    }
}

// 字段不存在异常
public class FieldNotFoundException extends FlexModelException {
    public FieldNotFoundException(String modelName, String fieldName) {
        super("Field not found: " + modelName + "." + fieldName);
    }
}
```

#### 异常处理示例

```java
try (Session session = sessionFactory.createSession("mySchema")) {
    DataOperations operations = session.getDataOperations();
    List<Map<String, Object>> results = operations.query("SELECT * FROM users");
} catch (FlexModelException e) {
    log.error("数据操作失败", e);
    // 处理异常
} catch (Exception e) {
    log.error("未知错误", e);
    // 处理其他异常
}
```

## 最佳实践

### 1. 资源管理

```java
// 使用try-with-resources确保资源释放
try (Session session = sessionFactory.createSession("mySchema")) {
    // 数据操作
} catch (Exception e) {
    // 异常处理
}
```

### 2. 批量操作

```java
// 使用批量操作提高性能
List<Map<String, Object>> users = generateUsers(1000);
operations.batchInsert("users", users);
```

### 3. 查询优化

```java
// 使用参数化查询避免SQL注入
operations.query("SELECT * FROM users WHERE age > ? AND status = ?", 
    Arrays.asList(18, "active"));

// 使用分页查询处理大量数据
Query query = new Query("SELECT * FROM users")
    .setPage(1)
    .setSize(100);
```

### 4. 缓存使用

```java
// 合理使用缓存提高性能
SessionFactory sessionFactory = SessionFactory.builder()
    .setDefaultDataSourceProvider(jdbcProvider)
    .setCache(new ConcurrentHashMapCache())
    .build();
```

### 5. 事务管理

```java
// 在事务中执行相关操作
try (Session session = sessionFactory.createSession("mySchema")) {
    session.beginTransaction();
    try {
        // 执行数据操作
        operations.insert("users", user);
        operations.insert("user_profiles", profile);
        
        session.commit();
    } catch (Exception e) {
        session.rollback();
        throw e;
    }
}
```

## 性能调优

### 1. 连接池配置

```java
HikariDataSource dataSource = new HikariDataSource();
dataSource.setMaximumPoolSize(20);
dataSource.setMinimumIdle(5);
dataSource.setConnectionTimeout(30000);
dataSource.setIdleTimeout(600000);
dataSource.setMaxLifetime(1800000);
```

### 2. 查询优化

```java
// 使用索引优化查询
Index nameIndex = new Index("users")
    .addField("name", Direction.ASC);
schemaOps.createIndex(nameIndex);

// 使用投影减少数据传输
Query query = new Query("SELECT id, name FROM users");
```

### 3. 缓存策略

```java
// 实现自定义缓存策略
public class LRUCache implements Cache {
    private final Map<String, Object> cache;
    private final int maxSize;
    
    public LRUCache(int maxSize) {
        this.maxSize = maxSize;
        this.cache = new LinkedHashMap<String, Object>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Object> eldest) {
                return size() > maxSize;
            }
        };
    }
    
    // 实现Cache接口方法...
}
```

## 扩展开发

### 1. 自定义数据源提供者

```java
public class CustomDataSourceProvider implements DataSourceProvider {
    private final String id;
    private final CustomDataSource dataSource;
    
    public CustomDataSourceProvider(String id, CustomDataSource dataSource) {
        this.id = id;
        this.dataSource = dataSource;
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public CustomDataSource getDataSource() {
        return dataSource;
    }
}
```

### 2. 自定义SQL方言

```java
public class CustomSqlDialect extends AbstractSqlDialect {
    @Override
    public String getTypeName(int code, long length, int precision, int scale) {
        switch (code) {
            case Types.VARCHAR:
                return "VARCHAR(" + length + ")";
            case Types.INTEGER:
                return "INT";
            case Types.DECIMAL:
                return "DECIMAL(" + precision + "," + scale + ")";
            default:
                return super.getTypeName(code, length, precision, scale);
        }
    }
    
    @Override
    public String getAddColumnString() {
        return "ADD COLUMN";
    }
    
    @Override
    public String getDropColumnString() {
        return "DROP COLUMN";
    }
}
```

### 3. 自定义字段类型

```java
public class CustomField extends AbstractField<CustomField, Object> {
    private String customProperty;
    
    public CustomField(String name) {
        super(name);
    }
    
    public CustomField setCustomProperty(String customProperty) {
        this.customProperty = customProperty;
        return this;
    }
    
    public String getCustomProperty() {
        return customProperty;
    }
    
    @Override
    public String getType() {
        return "CUSTOM";
    }
}
```

---

**更多API文档请参考各模块的具体文档。**