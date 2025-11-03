# FlexModel Quarkus扩展

FlexModel Quarkus扩展提供了在Quarkus应用中集成FlexModel Session管理的完整解决方案，支持自动Session生命周期管理、事务管理和异步操作中的上下文传播。

## 功能特性

- **自动Session管理**: 通过`@SessionManaged`注解自动管理Session的生命周期
- **事务管理**: 通过`@Transactional`注解自动管理事务边界
- **异步支持**: 自动在Mutiny、CompletableFuture等异步操作中传播Session上下文
- **CDI集成**: 完全集成Quarkus CDI容器，支持依赖注入
- **零配置**: 扩展自动注册所有必要的组件，无需手动配置

## 快速开始

### 1. 添加依赖

在`pom.xml`中添加依赖：

```xml
<dependency>
    <groupId>tech.wetech.flexmodel</groupId>
    <artifactId>flexmodel-quarkus</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>

<!-- Context Propagation支持异步操作 -->
<dependency>
    <groupId>io.smallrye</groupId>
    <artifactId>smallrye-context-propagation</artifactId>
</dependency>
```

### 2. 配置SessionFactory

创建SessionFactory的Producer：

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
}
```

### 3. 使用注解

#### @SessionManaged

用于标记需要自动Session管理的方法或类：

```java
@Path("/users")
@SessionManaged
public class UserResource {

  @Inject
  private Session session;

  @GET
  public Response getAllUsers() {
    // Session已自动创建，可以直接使用
    List<Map<String, Object>> users = session.data().find("users", Query.Builder.create().build());
    return Response.ok(users).build();
    // 方法结束时Session自动销毁
  }

  @POST
  @SessionManaged(schema = "custom") // 指定特定的schema
  public Response createUser(Map<String, Object> userData) {
    // 使用指定的schema创建Session
    session.data().insert("users", userData);
    return Response.ok().build();
  }
}
```

#### @Transactional

用于自动管理事务，必须配合`@SessionManaged`使用：

```java
@ApplicationScoped
@SessionManaged
public class UserService {

  @Inject
  private Session session;

  @Transactional
  public void createUser(Map<String, Object> userData) {
    // 事务已自动开启
    session.data().insert("users", userData);
    // 方法正常返回时自动提交，异常时自动回滚
  }

  @Transactional(readOnly = true)
  public List<Map<String, Object>> getAllUsers() {
    // 只读事务
    return session.data().find("users", Query.Builder.create().build());
  }
}
```

## 异步操作支持

扩展通过`smallrye-context-propagation`自动支持在异步操作中传播Session上下文，无需额外配置：

### Mutiny示例

```java
@SessionManaged
public class AsyncUserService {

  @Inject
  private Session session;

  @Inject
  private QuarkusSessionManager sessionManager;

  public Uni<List<Map<String, Object>>> getAllUsersAsync() {
    // Session上下文会自动传播到异步操作中
    return Uni.createFrom().item(() -> {
      // 在新线程中可以访问Session
      Session asyncSession = sessionManager.getCurrentSession();
      return asyncSession.data().find("users", Query.Builder.create().build());
    });
  }

  @Transactional
  public Uni<Void> createUserAsync(Map<String, Object> userData) {
    // 事务和Session都会自动传播
    return Uni.createFrom().item(() -> {
      Session asyncSession = sessionManager.getCurrentSession();
      asyncSession.data().insert("users", userData);
      return null;
    });
  }
}
```

### CompletableFuture示例

```java
@SessionManaged
public class AsyncService {

  @Inject
  private QuarkusSessionManager sessionManager;

  @Transactional
  public CompletableFuture<Void> processAsync() {
    return CompletableFuture.supplyAsync(() -> {
      // Session上下文自动传播
      Session session = sessionManager.getCurrentSession();
      session.data().insert("items", itemData);
      return null;
    });
  }
}
```

## 注解详解

### @SessionManaged

**作用**: 标记需要自动Session管理的方法或类

**属性**:
- `schema()`: 指定要使用的schema名称，默认为空字符串（使用默认schema）

**使用级别**:
- 类级别：类中所有方法都会自动管理Session
- 方法级别：只有标记的方法会管理Session，会覆盖类级别的设置

**注意事项**:
- Session在方法执行前创建，执行后销毁
- 支持嵌套调用，不会重复创建Session
- 自动激活RequestContext，确保CDI Bean可用

### @Transactional

**作用**: 自动管理事务边界

**属性**:
- `readOnly()`: 是否为只读事务，默认为`false`

**使用级别**:
- 类级别：类中所有方法都使用事务管理
- 方法级别：只有标记的方法使用事务管理

**注意事项**:
- 必须配合`@SessionManaged`使用
- 方法正常返回时自动提交事务
- 方法抛出异常时自动回滚事务
- 支持嵌套事务（但当前实现为简化版）

## 核心组件

### QuarkusSessionManager

继承自`SessionManager`，集成CDI容器：

```java
@ApplicationScoped
public class QuarkusSessionManager extends SessionManager {
  @Inject
  public QuarkusSessionManager(SessionFactory sessionFactory) {
    super(sessionFactory);
  }
}
```

### SessionProvider

CDI提供者，用于注入Session：

```java
@Inject
private Session session;
```

### SessionContextPropagator

上下文传播器，自动在异步操作间传播Session状态，无需手动配置。

## 最佳实践

1. **使用注解**: 优先使用`@SessionManaged`和`@Transactional`注解，而不是手动管理
2. **事务边界**: 使用`@Transactional`明确事务边界，避免手动管理事务
3. **异步操作**: 在异步操作中，Session上下文会自动传播，可以直接使用
4. **嵌套调用**: 支持嵌套调用，不会重复创建Session
5. **异常处理**: `@Transactional`会自动处理异常并回滚事务

## 技术原理

### 上下文传播机制

扩展实现了`ThreadContextProvider`接口，在异步操作切换线程时自动：
1. 捕获当前Session状态（schema列表和lastUsedSchema）
2. 在新线程中恢复Session状态
3. 通过SessionManager重新获取Session实例

### RequestContext激活

`SessionInterceptor`自动激活RequestContext，确保在异步操作中可以：
1. 访问CDI Bean
2. 注入依赖
3. 使用CDI的`@RequestScoped`等作用域

### 拦截器优先级

- `SessionInterceptor`: `APPLICATION + 100` (先执行，创建Session)
- `TransactionalInterceptor`: `APPLICATION + 50` (后执行，管理事务)

## 故障排查

### Session未创建

确保方法或类上使用了`@SessionManaged`注解。

### 事务未生效

确保：
1. 方法或类上使用了`@Transactional`注解
2. 同时使用了`@SessionManaged`注解
3. Session已经创建

### 异步操作中无法访问Session

确保：
1. 添加了`smallrye-context-propagation`依赖
2. 使用了支持上下文传播的异步操作（如Mutiny、CompletableFuture）
3. 方法上有`@SessionManaged`注解

## 版本兼容性

- Quarkus: 3.13.2+
- Java: 21+
- FlexModel Core: 0.0.1-SNAPSHOT

## 许可证

与FlexModel项目相同的许可证。

