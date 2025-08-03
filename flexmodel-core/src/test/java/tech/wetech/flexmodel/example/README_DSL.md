# FlexModel DSL 使用指南

## 概述

FlexModel提供了强大的DSL（Domain Specific Language）API，支持流畅的数据库操作。本文档介绍如何使用DSL进行插入、更新、删除和查询操作。

## 基本概念

### DSL构建器类型

1. **DSLQueryBuilder** - 查询构建器
2. **DSLInsertBuilder** - 插入构建器
3. **DSLUpdateBuilder** - 更新构建器
4. **DSLDeleteBuilder** - 删除构建器

### 支持的操作类型

- **字符串模型名操作** - 使用字符串指定模型名
- **实体类操作** - 使用Java实体类
- **类型化操作** - 提供类型安全的API

## 插入操作

### 使用字符串模型名

```java
// 插入单条记录
Map<String, Object> record = Map.of("name", "张三", "age", 25);
int rows = session.insert()
  .insertInto("users")
  .values(record)
  .execute();
```

### 使用实体类

```java
// 插入实体对象
User user = new User("李四", "lisi@example.com", 30);
int rows = session.insert()
  .insertInto(User.class)
  .values(user)
  .execute();
```

## 更新操作

### 单个字段更新

```java
// 使用set方法更新单个字段
int rows = session.update()
  .update("users")
  .set("name", "新名字")
  .where(field("id").eq(1))
  .execute();
```

### 多个字段更新

```java
// 使用values方法更新多个字段
Map<String, Object> values = Map.of("name", "新名字", "age", 26);
int rows = session.update()
  .update("users")
  .values(values)
  .where(field("id").eq(1))
  .execute();
```

### 按ID更新

```java
// 使用实体类按ID更新
User updateUser = new User("王五", "wangwu@example.com", 35);
int rows = session.update()
  .update(User.class)
  .values(updateUser)
  .whereId(1L)
  .execute();
```

## 删除操作

### 按ID删除

```java
// 按ID删除记录
int rows = session.delete()
  .deleteFrom("users")
  .whereId(1)
  .execute();
```

### 按条件删除

```java
// 按条件删除记录
int rows = session.delete()
  .deleteFrom("users")
  .where(field("age").lt(18))
  .execute();
```

### 删除所有记录

```java
// 删除所有记录（危险操作）
int rows = session.delete()
  .deleteFrom("users")
  .execute();
```

## 查询操作

### 基本查询

```java
// 查询所有记录
List<Map<String, Object>> allUsers = session.dsl()
  .from("users")
  .execute();

// 条件查询
List<User> activeUsers = session.dsl()
  .from(User.class)
  .where(field("status").eq("active"))
  .execute();
```

### 复杂查询

```java
// 多条件查询
List<User> users = session.dsl()
  .from(User.class)
  .where(field("age").gte(18)
    .and(field("status").eq("active"))
    .and(field("name").contains("张")))
  .orderBy("age", Session.Direction.DESC)
  .page(1, 10)
  .execute();
```

### 聚合查询

```java
// 统计记录数
long totalUsers = session.dsl()
  .from("users")
  .count();

// 条件统计
long activeUsers = session.dsl()
  .from("users")
  .where(field("status").eq("active"))
  .count();
```

## 条件表达式

### 基本条件

```java
// 等于
field("name").eq("张三")

// 不等于
field("status").ne("inactive")

// 大于
field("age").gt(18)

// 大于等于
field("age").gte(18)

// 小于
field("age").lt(65)

// 小于等于
field("age").lte(65)
```

### 字符串条件

```java
// 包含
field("name").contains("张")

// 开始于
field("name").startsWith("张")

// 结束于
field("name").endsWith("三")
```

### 组合条件

```java
// AND条件
field("age").gte(18).and(field("status").eq("active"))

// OR条件
field("status").eq("active").or(field("status").eq("pending"))

// 复杂嵌套条件
field("age").gte(18)
  .and(field("status").eq("active").or(field("status").eq("pending")))
  .and(field("name").contains("张"))
```

## 排序和分页

### 排序

```java
// 单字段排序
.orderBy("age", Session.Direction.DESC)

// 多字段排序
.orderBy("age", Session.Direction.DESC)
.orderBy("name", Session.Direction.ASC)
```

### 分页

```java
// 分页查询
.page(1, 10)  // 第1页，每页10条

// 限制结果数量
.limit(5)     // 限制返回5条记录
```

## 错误处理

### 常见错误

1. **模型名不存在**
   ```java
   try {
     session.insert().insertInto("nonexistent_table").values(data).execute();
   } catch (Exception e) {
     System.out.println("模型不存在: " + e.getMessage());
   }
   ```

2. **记录不存在**
   ```java
   // 更新不存在的记录会返回0
   int rows = session.update()
     .update("users")
     .set("name", "test")
     .whereId(999999L)
     .execute();
   // rows = 0 表示没有找到匹配的记录
   ```

## 最佳实践

### 1. 使用实体类进行类型安全操作

```java
// 推荐：使用实体类
List<User> users = session.dsl()
  .from(User.class)
  .where(field("status").eq("active"))
  .execute();

// 不推荐：使用字符串模型名（除非必要）
List<Map<String, Object>> users = session.dsl()
  .from("users")
  .where(field("status").eq("active"))
  .execute();
```

### 2. 合理使用条件组合

```java
// 推荐：清晰的条件组合
Predicate condition = field("age").gte(18)
  .and(field("status").eq("active"))
  .and(field("name").contains("张"));

List<User> users = session.dsl()
  .from(User.class)
  .where(condition)
  .execute();
```

### 3. 批量操作优化

```java
// 推荐：批量更新
int rows = session.update()
  .update(User.class)
  .set("status", "inactive")
  .where(field("lastLoginDate").lt(LocalDate.now().minusDays(30)))
  .execute();

// 不推荐：循环单个更新
for (User user : inactiveUsers) {
  session.update()
    .update(User.class)
    .set("status", "inactive")
    .whereId(user.getId())
    .execute();
}
```

### 4. 错误处理

```java
try {
  int rows = session.insert()
    .insertInto(User.class)
    .values(user)
    .execute();
  System.out.println("插入成功: " + rows + " 条记录");
} catch (Exception e) {
  System.err.println("插入失败: " + e.getMessage());
  // 进行适当的错误处理
}
```

## 完整示例

```java
public class UserService {
  private final Session session;
  
  public UserService(Session session) {
    this.session = session;
  }
  
  // 创建用户
  public int createUser(User user) {
    return session.insert()
      .insertInto(User.class)
      .values(user)
      .execute();
  }
  
  // 根据ID查找用户
  public User findById(Long id) {
    return session.dsl()
      .from(User.class)
      .whereId(id)
      .executeOne();
  }
  
  // 查找活跃用户
  public List<User> findActiveUsers() {
    return session.dsl()
      .from(User.class)
      .where(field("status").eq("active"))
      .orderBy("createTime", Session.Direction.DESC)
      .execute();
  }
  
  // 更新用户信息
  public int updateUser(Long id, User user) {
    return session.update()
      .update(User.class)
      .values(user)
      .whereId(id)
      .execute();
  }
  
  // 删除用户
  public int deleteUser(Long id) {
    return session.delete()
      .deleteFrom(User.class)
      .whereId(id)
      .execute();
  }
  
  // 分页查询用户
  public List<User> findUsersByPage(int page, int size) {
    return session.dsl()
      .from(User.class)
      .orderBy("id", Session.Direction.DESC)
      .page(page, size)
      .execute();
  }
}
```

## 总结

FlexModel的DSL API提供了强大而灵活的数据库操作能力。通过合理使用这些API，可以编写出简洁、类型安全、易于维护的数据库操作代码。建议在实际项目中根据具体需求选择合适的API组合。 
