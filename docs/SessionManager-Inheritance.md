# SessionManager 继承设计

## 概述

本文档说明了如何使用继承的方式来实现Quarkus集成，通过继承通用的 `SessionManager` 类来复用代码，避免重复实现。

## 设计优势

### 1. 代码复用

通过继承通用的 `SessionManager`，Quarkus特定的实现可以复用所有核心功能：

```java
// 通用SessionManager (flexmodel-core)
public class SessionManager {
    protected SessionFactory sessionFactory;
    protected final ThreadLocal<Session> sessionHolder = new ThreadLocal<>();
    
    public Session getSession(String schemaName) { /* 实现 */ }
    public void closeSession() { /* 实现 */ }
    // ... 其他方法
}

// Quarkus特定实现 (flexmodel-quarkus-it)
@ApplicationScoped
public class QuarkusSessionManager extends SessionManager {
    @Inject
    public QuarkusSessionManager(SessionFactory sessionFactory) {
        super(sessionFactory);
    }
    // 无需重复实现核心功能
}
```

### 2. 职责分离

- **SessionManager**: 负责通用的Session生命周期管理
- **QuarkusSessionManager**: 负责CDI集成和Quarkus特定的配置

### 3. 易于扩展

其他框架可以轻松创建自己的SessionManager实现：

```java
// Spring Boot集成示例
@Component
public class SpringSessionManager extends SessionManager {
    @Autowired
    public SpringSessionManager(SessionFactory sessionFactory) {
        super(sessionFactory);
    }
}

// Vert.x集成示例
public class VertxSessionManager extends SessionManager {
    public VertxSessionManager(SessionFactory sessionFactory) {
        super(sessionFactory);
    }
}
```

## 实现细节

### 1. 通用SessionManager设计

```java
public class SessionManager {
    // 使用protected访问级别，允许子类访问
    protected SessionFactory sessionFactory;
    protected final ThreadLocal<Session> sessionHolder = new ThreadLocal<>();
    
    // 提供默认构造函数，支持继承
    public SessionManager() {
        // 用于继承
    }
    
    // 提供带参数的构造函数
    public SessionManager(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    // 核心方法实现
    public Session getSession(String schemaName) {
        Session session = sessionHolder.get();
        if (session == null) {
            session = sessionFactory.createSession(schemaName);
            sessionHolder.set(session);
        }
        return session;
    }
    
    public void closeSession() {
        Session session = sessionHolder.get();
        if (session != null) {
            try {
                session.close();
            } finally {
                sessionHolder.remove();
            }
        }
    }
    
    // 其他方法...
}
```

### 2. Quarkus集成实现

```java
@ApplicationScoped
public class QuarkusSessionManager extends SessionManager {
    
    @Inject
    public QuarkusSessionManager(SessionFactory sessionFactory) {
        super(sessionFactory);
    }
    
    // 可以添加Quarkus特定的方法
    public void someQuarkusSpecificMethod() {
        // Quarkus特定的实现
    }
}
```

## 配置方式

### 1. 使用QuarkusSessionManager

```java
@ApplicationScoped
public class SessionConfig {
    
    @Produces
    @ApplicationScoped
    public SessionFactory sessionFactory() {
        // 配置SessionFactory
        return SessionFactory.builder()
            .setDefaultDataSourceProvider(/* ... */)
            .build();
    }
    
    // QuarkusSessionManager会自动被CDI管理
    // 无需额外配置
}
```

### 2. 使用通用SessionManager

```java
@ApplicationScoped
public class SessionConfig {
    
    @Produces
    @ApplicationScoped
    public SessionManager sessionManager(SessionFactory sessionFactory) {
        return new SessionManager(sessionFactory);
    }
}
```

## 使用示例

### 1. 在服务层中使用

```java
@ApplicationScoped
@SessionManaged
public class UserService {
    
    @Inject
    private Session session; // 自动注入当前请求的Session
    
    public List<Map<String, Object>> getAllUsers() {
        return session.data().find("users", Query.Builder.create().build());
    }
}
```

### 2. 在REST资源中使用

```java
@Path("/users")
@SessionManaged
public class UserResource {
    
    @Inject
    private UserService userService; // 注入服务层
    
    @GET
    public Response getAllUsers() {
        List<Map<String, Object>> users = userService.getAllUsers();
        return Response.ok(users).build();
    }
}
```

## 优势总结

1. **代码复用**: 避免重复实现Session管理逻辑
2. **维护性**: 核心逻辑集中在一个地方，易于维护
3. **扩展性**: 其他框架可以轻松创建自己的实现
4. **一致性**: 所有实现都遵循相同的接口和行为
5. **测试性**: 可以独立测试通用的SessionManager逻辑

## 注意事项

1. **字段访问**: 使用 `protected` 访问级别，允许子类访问但保持封装
2. **构造函数**: 提供默认构造函数支持继承
3. **依赖注入**: 子类通过构造函数注入依赖
4. **线程安全**: ThreadLocal的使用确保了线程安全

这种继承设计模式提供了一个清晰、可维护、可扩展的Session管理解决方案。 