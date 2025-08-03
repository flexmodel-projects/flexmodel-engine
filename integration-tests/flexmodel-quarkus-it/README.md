# FlexModel Quarkus 集成

这个模块提供了FlexModel与Quarkus框架的集成，实现了自动的Session生命周期管理。

## 功能特性

- **自动Session管理**: 在请求开始时自动创建Session，在请求结束时自动销毁Session
- **事务支持**: 支持手动事务管理（开始、提交、回滚）
- **CDI集成**: 通过CDI容器提供Session实例
- **拦截器支持**: 使用注解标记需要Session管理的方法

## 核心组件

### 1. SessionManager (flexmodel-core)

通用的Session管理器，使用ThreadLocal存储Session实例。

```java
public class SessionManager {
  // 获取当前请求的Session
  public Session getSession(String schemaName);

  public Session getSession(); // 使用默认模式

  // 关闭当前请求的Session
  public void closeSession();

  // 检查Session状态
  public boolean hasActiveSession();

  public Session getCurrentSession();
}
```

### 2. QuarkusSessionManager

Quarkus特定的Session管理器，继承自SessionManager，集成CDI容器。

```java

@ApplicationScoped
public class QuarkusSessionManager extends SessionManager {
  @Inject
  public QuarkusSessionManager(SessionFactory sessionFactory) {
    super(sessionFactory);
  }
}
```

### 2. SessionManaged 注解

用于标记需要自动Session管理的方法或类。

```java
@SessionManaged // 类级别，所有方法都会自动管理Session
public class UserResource {
    
    @SessionManaged(schema = "custom") // 方法级别，指定模式
    public Response createUser() {
        // 方法执行时会自动创建Session，执行完成后自动销毁
    }
}
```

### 3. SessionInterceptor

拦截器，负责在方法执行前后管理Session生命周期。

### 4. SessionProvider

CDI提供者，用于在容器中提供Session实例。

## 使用方法

### 1. 配置SessionFactory和SessionManager

在应用启动时配置SessionFactory和SessionManager：

```java

@ApplicationScoped
public class SessionConfig {

  @Produces
  @ApplicationScoped
  public SessionFactory sessionFactory() {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setJdbcUrl("jdbc:sqlite:file::memory:?cache=shared");

    return SessionFactory.builder()
      .setDefaultDataSourceProvider(new JdbcDataSourceProvider("system", dataSource))
      .build();
  }

  // 可选：配置通用的SessionManager
  @Produces
  @ApplicationScoped
  public SessionManager sessionManager(SessionFactory sessionFactory) {
    return new SessionManager(sessionFactory);
  }
}
```

### 2. 在REST资源中使用

```java

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@SessionManaged // 类级别Session管理
public class UserResource {

  @Inject
  private Session session; // 自动注入当前请求的Session

  @GET
  public Response getAllUsers() {
    // Session已自动创建
    List<Map<String, Object>> users = session.data().find("users", Query.Builder.create().build());
    return Response.ok(users).build();
    // 方法结束时Session自动销毁
  }

  @POST
  @SessionManaged(schema = "custom") // 方法级别，指定模式
  public Response createUser(Map<String, Object> userData) {
    // 手动事务管理
    session.startTransaction();
    try {
      int affectedRows = session.data().insert("users", userData);
      session.commit();
      return Response.status(Response.Status.CREATED).build();
    } catch (Exception e) {
      session.rollback();
      throw e;
    }
  }
}
```

### 3. 在服务类中使用

```java

@ApplicationScoped
@SessionManaged
public class UserService {

  @Inject
  private Session session;

  public List<Map<String, Object>> getAllUsers() {
    return session.data().find("users", Query.Builder.create().build());
  }

  public Map<String, Object> getUserById(String id) {
    return session.data().findById("users", id);
  }

  public Map<String, Object> createUser(Map<String, Object> userData) {
    session.startTransaction();
    try {
      int affectedRows = session.data().insert("users", userData);
      session.commit();

      Map<String, Object> result = new HashMap<>();
      result.put("message", "User created successfully");
      result.put("affectedRows", affectedRows);
      return result;
    } catch (Exception e) {
      session.rollback();
      throw new RuntimeException("Failed to create user", e);
    }
  }
}
```

### 4. 在REST资源中使用服务层

```java

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@SessionManaged
public class UserResource {

  @Inject
  private UserService userService;

  @GET
  public Response getAllUsers() {
    try {
      List<Map<String, Object>> users = userService.getAllUsers();
      return Response.ok(users).build();
    } catch (Exception e) {
      return Response.serverError().entity("Error getting users: " + e.getMessage()).build();
    }
  }

  @POST
  public Response createUser(Map<String, Object> userData) {
    try {
      Map<String, Object> result = userService.createUser(userData);
      return Response.status(Response.Status.CREATED).entity(result).build();
    } catch (Exception e) {
      return Response.serverError().entity("Error creating user: " + e.getMessage()).build();
    }
  }
}
```

## 事务管理

Session支持手动事务管理：

```java
@SessionManaged
public class TransactionalService {
    
    @Inject
    private Session session;
    
    public void performTransactionalOperation() {
        session.startTransaction();
        try {
            // 执行多个操作
            session.data().insert("users", user1);
            session.data().insert("users", user2);
            
            session.commit(); // 提交事务
        } catch (Exception e) {
            session.rollback(); // 回滚事务
            throw e;
        }
    }
}
```

## 配置选项

### 1. 默认模式

如果不指定模式，系统会使用第一个可用的模式作为默认值：

```java
@SessionManaged // 使用默认模式
public class DefaultSchemaService {
    // ...
}
```

### 2. 指定模式

可以通过注解参数指定特定的模式：

```java
@SessionManaged(schema = "user_schema")
public class UserSchemaService {
    // ...
}
```

### 3. 方法级别覆盖

方法级别的注解会覆盖类级别的设置：

```java
@SessionManaged(schema = "default")
public class MultiSchemaService {
    
    @SessionManaged(schema = "user_schema") // 覆盖类级别的设置
    public void userOperation() {
        // 使用user_schema模式
    }
    
    public void defaultOperation() {
        // 使用类级别的default模式
    }
}
```

## 最佳实践

1. **使用注解**: 优先使用`@SessionManaged`注解而不是手动管理Session
2. **继承设计**: 通过继承通用的SessionManager来复用代码，避免重复实现
3. **分层架构**: 使用服务层封装业务逻辑，资源层处理HTTP请求
4. **事务边界**: 在需要事务的方法中明确管理事务边界
5. **异常处理**: 在事务操作中正确处理异常并回滚事务
6. **资源清理**: 拦截器会自动清理Session，无需手动关闭
7. **模式管理**: 合理规划和使用不同的数据模式

## 注意事项

1. Session是线程安全的，每个请求都有独立的Session实例
2. 拦截器会自动处理Session的创建和销毁，避免内存泄漏
3. 事务必须在同一个Session实例中完成
4. 异常情况下Session会自动关闭，确保资源释放

## 示例项目

完整的示例代码请参考 `UserResource.java` 文件，展示了完整的CRUD操作和事务管理。
