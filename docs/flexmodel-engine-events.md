# Flexmodel 事件功能

Flexmodel 事件功能提供了前置事件（PreChange）和后置事件（Changed）的支持，让你可以在数据变更的各个阶段进行干预和处理。

## 功能特性

- **前置事件（PreChange）**：在数据操作执行前触发，可以记录日志、验证数据等
- **后置事件（Changed）**：在数据操作执行后触发，可以执行后续操作如通知、缓存更新等
- **事件类型**：支持 INSERT、UPDATE、DELETE 操作的前置和后置事件
- **优先级支持**：监听器支持优先级排序
- **异常隔离**：监听器中的异常不会影响其他监听器和主业务流程
- **异步支持**：支持异步事件处理

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
// 前置事件监听器
eventPublisher.addPreChangeListener(new PreChangeEventListener() {
    @Override
    public void onPreChange(PreChangeEvent event) {
        System.out.println("前置事件: " + event.getEventType() + " - " + event.getModelName());
    }
    
    @Override
    public boolean supports(String eventType) {
        return eventType.startsWith("PRE_");
    }
    
    @Override
    public int getOrder() {
        return 100; // 优先级
    }
});

// 后置事件监听器
eventPublisher.addChangedListener(new ChangedEventListener() {
    @Override
    public void onChanged(ChangedEvent event) {
        System.out.println("后置事件: " + event.getEventType() + " - " + event.getModelName());
    }
    
    @Override
    public boolean supports(String eventType) {
        return eventType.endsWith("ED");
    }
    
    @Override
    public int getOrder() {
        return 200;
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

## 事件对象

### PreChangeEvent（前置事件）
```java
public interface PreChangeEvent extends FlexmodelEvent {
    Object getNewData();  // 新数据
    Object getId();       // 记录ID
}
```

### ChangedEvent（后置事件）
```java
public interface ChangedEvent extends FlexmodelEvent {
    Object getOldData();      // 旧数据
    Object getNewData();      // 新数据
    Object getId();           // 记录ID
    int getAffectedRows();    // 影响行数
    boolean isSuccess();      // 是否成功
    Throwable getException(); // 异常（如果失败）
}
```

### FlexmodelEvent（基础事件）
```java
public interface FlexmodelEvent {
    String getEventType();    // 事件类型
    String getModelName();    // 模型名称
    String getSchemaName();   // 模式名称
    long getTimestamp();      // 时间戳
    Object getSource();       // 事件源
    String getSessionId();    // 会话ID
}
```

## 监听器接口

### PreChangeEventListener
```java
public interface PreChangeEventListener {
    void onPreChange(PreChangeEvent event);
    boolean supports(String eventType);
    int getOrder(); // 优先级，数字越小优先级越高
}
```

### ChangedEventListener
```java
public interface ChangedEventListener {
    void onChanged(ChangedEvent event);
    boolean supports(String eventType);
    int getOrder();
}
```

### FlexmodelEventListener（通用监听器）
```java
public interface FlexmodelEventListener {
    void onPreChange(PreChangeEvent event);
    void onChanged(ChangedEvent event);
    boolean supportsPreChange(String eventType);
    boolean supportsChanged(String eventType);
    int getOrder();
}
```

## 实际应用场景

### 1. 审计日志
```java
public class AuditLogListener implements FlexmodelEventListener {
    @Override
    public void onPreChange(PreChangeEvent event) {
        auditLog.info("操作开始 - 模型: {}, 操作: {}", event.getModelName(), event.getEventType());
    }
    
    @Override
    public void onChanged(ChangedEvent event) {
        if (event.isSuccess()) {
            auditLog.info("操作成功 - 模型: {}, 影响行数: {}", event.getModelName(), event.getAffectedRows());
        } else {
            auditLog.error("操作失败 - 模型: {}, 错误: {}", event.getModelName(), event.getException().getMessage());
        }
    }
    
    // 其他方法实现...
}
```

### 2. 缓存更新
```java
public class CacheUpdateListener implements ChangedEventListener {
    @Override
    public void onChanged(ChangedEvent event) {
        if (event.isSuccess()) {
            // 异步更新缓存
            CompletableFuture.runAsync(() -> {
                updateCache(event.getModelName(), event.getNewData());
            });
        }
    }
    
    // 其他方法实现...
}
```

### 3. 业务规则验证
```java
public class BusinessRuleListener implements PreChangeEventListener {
    @Override
    public void onPreChange(PreChangeEvent event) {
        if ("users".equals(event.getModelName())) {
            validateUserData(event.getNewData());
        }
    }
    
    private void validateUserData(Object data) {
        // 实现业务规则验证
        if (data instanceof Map) {
            Map<String, Object> userData = (Map<String, Object>) data;
            String email = (String) userData.get("email");
            if (email != null && !email.contains("@")) {
                throw new IllegalArgumentException("邮箱格式不正确");
            }
        }
    }
    
    // 其他方法实现...
}
```

### 4. 通知发送
```java
public class NotificationListener implements ChangedEventListener {
    @Override
    public void onChanged(ChangedEvent event) {
        if (event.isSuccess()) {
            // 异步发送通知
            CompletableFuture.runAsync(() -> {
                sendNotification(event);
            });
        }
    }
    
    // 其他方法实现...
}
```

## 注意事项

1. **异常处理**：监听器中的异常不会影响其他监听器和主业务流程
2. **性能考虑**：监听器中的操作应该尽量快速，耗时操作建议异步处理
3. **优先级**：监听器按优先级排序执行，数字越小优先级越高
4. **批量操作**：批量更新和删除操作只触发后置事件，不触发前置事件
5. **事务支持**：事件在事务提交后触发，确保数据一致性

## 扩展功能

事件系统设计为可扩展的，你可以：

1. 实现自定义的EventPublisher
2. 添加新的事件类型
3. 集成消息队列进行分布式事件处理
4. 添加事件过滤和路由功能
5. 实现事件持久化存储
