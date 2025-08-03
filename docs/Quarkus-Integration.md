# FlexModel Quarkus 集成方案

## 概述

本文档描述了为FlexModel引擎提供的Quarkus集成方案，实现了自动的Session生命周期管理，让开发者可以专注于业务逻辑而无需手动管理Session的创建和销毁。

## 架构设计

### 核心组件

1. **SessionManager** (flexmodel-core)
  - 通用的Session管理器
  - 使用ThreadLocal存储Session实例
  - 提供Session的创建、获取、关闭功能

2. **QuarkusSessionManager** (flexmodel-quarkus-it)
  - Quarkus特定的Session管理器
  - 继承通用SessionManager的功能
  - 集成CDI容器

3. **SessionManaged** 注解
  - 标记需要自动Session管理的方法或类
  - 支持指定特定的数据模式
  - 支持类级别和方法级别的配置

4. **SessionInterceptor** 拦截器
  - 在方法执行前自动创建Session
  - 在方法执行后自动销毁Session
  - 确保资源的正确释放

5. **SessionProvider** CDI提供者
  - 在CDI容器中提供Session实例
  - 支持RequestScoped的Session生命周期

## 实现细节

### 1. Session生命周期管理

```java
@AroundInvoke
public Object aroundInvoke(InvocationContext context) throws Exception {
    // 获取SessionManaged注解
    SessionManaged sessionManaged = context.getMethod().getAnnotation(SessionManaged.class);
    if (sessionManaged == null) {
        sessionManaged = context.getTarget().getClass().getAnnotation(SessionManaged.class);
    }
    
    // 根据注解参数创建Session
    if (sessionManaged != null && !sessionManaged.schema().isEmpty()) {
        sessionManager.getSession(sessionManaged.schema());
    } else {
        sessionManager.getSession(); // 使用默认Session
    }
    
    try {
        // 执行被拦截的方法
        Object result = context.proceed();
        return result;
    } finally {
        // 确保在方法执行完成后关闭Session
        sessionManager.closeSession();
    }
}
```

### 2. ThreadLocal Session存储

```java
public class SessionManager {
    private final ThreadLocal<Session> sessionHolder = new ThreadLocal<>();
    
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
}
```

### 3. CDI集成

```java
@ApplicationScoped
public class QuarkusSessionManager {
    @Inject
    private SessionFactory sessionFactory;
    
    // 提供Session实例给CDI容器
    @Produces
    @RequestScoped
    public Session provideSession() {
        return getSession();
    }
}
```

## 使用方式

### 1. 基本使用

```java
@Path("/users")
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
}
```

### 2. 指定数据模式

```java
@SessionManaged(schema = "user_schema")
public class UserService {
    
    @Inject
    private Session session;
    
    public void createUser(Map<String, Object> userData) {
        // 使用user_schema模式的Session
        session.data().insert("users", userData);
    }
}
```

### 3. 事务管理

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
            
            session.commit();
        } catch (Exception e) {
            session.rollback();
            throw e;
        }
    }
}
```

## 配置说明

### 1. SessionFactory配置

```java
@ApplicationScoped
public class SessionFactoryConfig {
    
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

### 2. 拦截器配置

拦截器会自动注册到CDI容器中，无需额外配置。

## 优势

1. **自动化管理**: 无需手动创建和销毁Session
2. **线程安全**: 每个请求都有独立的Session实例
3. **资源管理**: 自动处理Session的清理，避免内存泄漏
4. **事务支持**: 完整的事务管理支持
5. **CDI集成**: 与Quarkus的CDI容器完美集成
6. **灵活性**: 支持多种配置方式和数据模式

## 注意事项

1. **Session范围**: Session是RequestScoped，每个HTTP请求都有独立的Session
2. **事务边界**: 事务必须在同一个Session实例中完成
3. **异常处理**: 拦截器会确保在异常情况下也正确关闭Session
4. **性能考虑**: ThreadLocal的使用确保了Session的快速访问

## 扩展性

该方案具有良好的扩展性：

1. **多数据源支持**: 可以轻松扩展到支持多个数据源
2. **自定义拦截器**: 可以创建自定义的拦截器来扩展功能
3. **监控集成**: 可以集成监控和日志功能
4. **缓存支持**: 可以集成缓存机制来提升性能

## 总结

这个Quarkus集成方案提供了一个完整、易用、高效的Session管理解决方案，让开发者可以专注于业务逻辑的实现，而无需关心Session的生命周期管理。通过注解驱动的方式，代码更加简洁，维护性更好。 
