# FlexModel 事件功能

FlexModel 事件功能提供了前置事件（PreChange）和后置事件（Changed）的支持，让你可以在数据变更的各个阶段进行干预和处理。

## 功能特性

- **前置事件（PreChange）**：在数据操作执行前触发，可以记录日志、验证数据等
- **后置事件（Changed）**：在数据操作执行后触发，可以执行后续操作如通知、缓存更新等
- **事件类型**：支持 INSERT、UPDATE、DELETE 操作的前置和后置事件
- **统一监听器接口**：使用统一的 `EventListener` 接口处理前置和后置事件
- **优先级支持**：监听器支持优先级排序，数字越小优先级越高
- **异常隔离**：监听器中的异常不会影响其他监听器和主业务流程
- **线程安全**：使用 `CopyOnWriteArrayList` 确保线程安全
- **事件过滤**：监听器可以指定支持的事件类型

## 事件类型

### 前置事件
- `PRE_INSERT`：插入操作前
- `PRE_UPDATE`：更新操作前  
- `PRE_DELETE`：删除操作前

### 后置事件
- `INSERTED`：插入操作后
- `UPDATED`：更新操作后
- `DELETED`：删除操作后

## 使用方法

### 1. 创建事件发布器

```java
EventPublisher eventPublisher = new SimpleEventPublisher();
```

### 2. 添加事件监听器

```java
// 统一的事件监听器
eventPublisher.addListener(new EventListener() {
    @Override
    public void onPreChange(PreChangeEvent event) {
        System.out.println("前置事件: " + event.getEventType() + " - " + event.getModelName());
    }
    
    @Override
    public void onChanged(ChangedEvent event) {
        System.out.println("后置事件: " + event.getEventType() + " - " + event.getModelName());
    }
    
    @Override
    public boolean supports(String eventType) {
        // 支持所有事件类型，或者可以指定特定类型
        return true;
    }
    
    @Override
    public int getOrder() {
        return 100; // 优先级，数字越小优先级越高
    }
});
```

### 3. 配置SessionFactory

```java
SessionFactory sessionFactory = SessionFactory.builder()
    .setEventPublisher(eventPublisher)
    .setDefaultDataSourceProvider(dataSourceProvider)
    .build();
```

### 4. 使用Session进行数据操作

```java
try (Session session = sessionFactory.createSession()) {
    // 插入数据 - 会触发PRE_INSERT和INSERTED事件
    Map<String, Object> user = Map.of("id", 1, "name", "张三");
    session.data().insert("users", user);
    
    // 更新数据 - 会触发PRE_UPDATE和UPDATED事件
    Map<String, Object> updateData = Map.of("name", "张三三");
    session.data().updateById("users", updateData, 1);
    
    // 删除数据 - 会触发PRE_DELETE和DELETED事件
    session.data().deleteById("users", 1);
}
```

### 5. 手动发布事件示例

```java
// 创建事件发布器
EventPublisher eventPublisher = new SimpleEventPublisher();

// 添加监听器
eventPublisher.addListener(new EventListener() {
    @Override
    public void onPreChange(PreChangeEvent event) {
        System.out.println("前置事件: " + event.getEventType() + " - " + event.getModelName());
    }
    
    @Override
    public void onChanged(ChangedEvent event) {
        System.out.println("后置事件: " + event.getEventType() + " - " + event.getModelName());
    }
    
    @Override
    public boolean supports(String eventType) {
        return true;
    }
    
    @Override
    public int getOrder() {
        return 100;
    }
});

// 手动发布前置插入事件
PreInsertEvent preInsertEvent = new PreInsertEvent(
    "users", "public", 
    Map.of("id", 1, "name", "张三"), 1, 
    "session-123", this
);
eventPublisher.publishPreChangeEvent(preInsertEvent);

// 手动发布后置插入事件
InsertedEvent insertedEvent = new InsertedEvent(
    "users", "public",
    null, Map.of("id", 1, "name", "张三"), 1,
    1, true, null, "session-123", this
);
eventPublisher.publishChangedEvent(insertedEvent);
```

## 事件对象

### BaseEvent（基础事件）
```java
public abstract class BaseEvent {
    public String getEventType();    // 事件类型字符串
    public String getModelName();    // 模型名称
    public String getSchemaName();   // 模式名称
    public long getTimestamp();      // 时间戳
    public Object getSource();       // 事件源
    public String getSessionId();    // 会话ID
    public EventType getEventTypeEnum(); // 事件类型枚举
}
```

### PreChangeEvent（前置事件）
```java
public abstract class PreChangeEvent extends BaseEvent {
    public Object getNewData();  // 新数据
    public Object getId();       // 记录ID
    public Object getOldData();  // 旧数据（更新和删除时）
}
```

### ChangedEvent（后置事件）
```java
public abstract class ChangedEvent extends BaseEvent {
    public Object getOldData();      // 旧数据
    public Object getNewData();      // 新数据
    public Object getId();           // 记录ID
    public int getAffectedRows();    // 影响行数
    public boolean isSuccess();      // 是否成功
    public Throwable getException(); // 异常（如果失败）
}
```

### 具体事件实现类

#### 前置事件实现
- `PreInsertEvent`：插入前置事件
- `PreUpdateEvent`：更新前置事件  
- `PreDeleteEvent`：删除前置事件

#### 后置事件实现
- `InsertedEvent`：插入完成事件
- `UpdatedEvent`：更新完成事件
- `DeletedEvent`：删除完成事件

## 监听器接口

### EventListener（统一监听器接口）
```java
public interface EventListener {
    void onPreChange(PreChangeEvent event);    // 处理前置事件
    void onChanged(ChangedEvent event);       // 处理后置事件
    boolean supports(String eventType);       // 是否支持指定事件类型
    int getOrder();                           // 优先级，数字越小优先级越高
}
```

### EventPublisher（事件发布器接口）
```java
public interface EventPublisher {
    void publishPreChangeEvent(PreChangeEvent event);  // 发布前置事件
    void publishChangedEvent(ChangedEvent event);      // 发布后置事件
    void addListener(EventListener listener);           // 添加监听器
    void removeListener(EventListener listener);        // 移除监听器
}
```

### SimpleEventPublisher（默认实现）
`SimpleEventPublisher` 是 `EventPublisher` 的默认实现，提供以下特性：
- 使用 `CopyOnWriteArrayList` 确保线程安全
- 支持监听器优先级排序
- 异常隔离：监听器异常不影响其他监听器
- 支持事件类型过滤

## 实际应用场景

### 1. 审计日志
```java
public class AuditLogListener implements EventListener {
    private static final Logger auditLog = LoggerFactory.getLogger("audit");
    
    @Override
    public void onPreChange(PreChangeEvent event) {
        auditLog.info("操作开始 - 模型: {}, 操作: {}, 会话: {}", 
            event.getModelName(), event.getEventType(), event.getSessionId());
    }
    
    @Override
    public void onChanged(ChangedEvent event) {
        if (event.isSuccess()) {
            auditLog.info("操作成功 - 模型: {}, 影响行数: {}", 
                event.getModelName(), event.getAffectedRows());
        } else {
            auditLog.error("操作失败 - 模型: {}, 错误: {}", 
                event.getModelName(), event.getException().getMessage());
        }
    }
    
    @Override
    public boolean supports(String eventType) {
        return true; // 支持所有事件类型
    }
    
    @Override
    public int getOrder() {
        return 1; // 高优先级，优先记录日志
    }
}
```

### 2. 缓存更新
```java
public class CacheUpdateListener implements EventListener {
    private final CacheManager cacheManager;
    
    @Override
    public void onPreChange(PreChangeEvent event) {
        // 前置事件不处理缓存
    }
    
    @Override
    public void onChanged(ChangedEvent event) {
        if (event.isSuccess()) {
            // 异步更新缓存
            CompletableFuture.runAsync(() -> {
                updateCache(event.getModelName(), event.getNewData());
            });
        }
    }
    
    @Override
    public boolean supports(String eventType) {
        // 只处理后置事件
        return "INSERTED".equals(eventType) || 
               "UPDATED".equals(eventType) || 
               "DELETED".equals(eventType);
    }
    
    @Override
    public int getOrder() {
        return 100; // 较低优先级
    }
    
    private void updateCache(String modelName, Object data) {
        // 实现缓存更新逻辑
    }
}
```

### 3. 业务规则验证
```java
public class BusinessRuleListener implements EventListener {
    @Override
    public void onPreChange(PreChangeEvent event) {
        if ("users".equals(event.getModelName())) {
            validateUserData(event.getNewData());
        }
    }
    
    @Override
    public void onChanged(ChangedEvent event) {
        // 后置事件不处理业务规则验证
    }
    
    @Override
    public boolean supports(String eventType) {
        // 只处理前置事件
        return "PRE_INSERT".equals(eventType) || 
               "PRE_UPDATE".equals(eventType);
    }
    
    @Override
    public int getOrder() {
        return 10; // 高优先级，优先验证
    }
    
    private void validateUserData(Object data) {
        if (data instanceof Map) {
            Map<String, Object> userData = (Map<String, Object>) data;
            String email = (String) userData.get("email");
            if (email != null && !email.contains("@")) {
                throw new IllegalArgumentException("邮箱格式不正确");
            }
        }
    }
}
```

### 4. 通知发送
```java
public class NotificationListener implements EventListener {
    private final NotificationService notificationService;
    
    @Override
    public void onPreChange(PreChangeEvent event) {
        // 前置事件不发送通知
    }
    
    @Override
    public void onChanged(ChangedEvent event) {
        if (event.isSuccess()) {
            // 异步发送通知
            CompletableFuture.runAsync(() -> {
                sendNotification(event);
            });
        }
    }
    
    @Override
    public boolean supports(String eventType) {
        // 只处理特定模型的后置事件
        return "INSERTED".equals(eventType) || 
               "UPDATED".equals(eventType) || 
               "DELETED".equals(eventType);
    }
    
    @Override
    public int getOrder() {
        return 200; // 低优先级，最后处理
    }
    
    private void sendNotification(ChangedEvent event) {
        // 实现通知发送逻辑
    }
}
```

## 注意事项

1. **异常处理**：监听器中的异常不会影响其他监听器和主业务流程，异常会被记录到日志中
2. **性能考虑**：监听器中的操作应该尽量快速，耗时操作建议异步处理
3. **优先级**：监听器按优先级排序执行，数字越小优先级越高
4. **线程安全**：`SimpleEventPublisher` 使用 `CopyOnWriteArrayList` 确保线程安全
5. **事件过滤**：监听器可以通过 `supports()` 方法指定支持的事件类型
6. **批量操作**：批量更新和删除操作只触发后置事件，不触发前置事件
7. **事务支持**：事件在事务提交后触发，确保数据一致性
8. **内存管理**：监听器会持有事件对象的引用，注意避免内存泄漏

## 扩展功能

事件系统设计为可扩展的，你可以：

1. **自定义EventPublisher**：实现 `EventPublisher` 接口，添加异步处理、消息队列等特性
2. **添加新的事件类型**：扩展 `EventType` 枚举，创建对应的事件实现类
3. **集成消息队列**：在监听器中集成 RabbitMQ、Kafka 等消息队列进行分布式事件处理
4. **添加事件过滤和路由功能**：基于模型名称、事件类型等条件进行事件路由
5. **实现事件持久化存储**：将事件存储到数据库或文件系统中
6. **添加事件重试机制**：对于失败的事件处理添加重试逻辑
7. **实现事件聚合**：将多个相关事件聚合成一个复合事件
