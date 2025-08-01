# FlexModel DSL 使用指南

## 概述

FlexModel DSL 提供了一种简洁、类型安全的数据操作语法，让您可以像编写SQL一样进行数据查询，同时保持代码的可读性和可维护性。

## 基本用法

### 1. 创建DSL查询构建器

```java
// 通过session创建DSL构建器
DSLQueryBuilder dsl = session.dsl();
```

### 2. 基本查询语法

```java
// 基本查询
List<Map<String, Object>> users = session.dsl()
    .select("id", "name", "email")
    .from("users")
    .where(Expressions.field("age").gt(18))
    .orderBy("name", Session.Direction.ASC)
    .page(1, 10)
    .execute();
```

### 3. 使用实体类查询

```java
// 使用实体类和方法引用
List<User> userList = session.dsl()
    .from(User.class)
    .where(Expressions.field(User::getName).eq("张三"))
    .execute();
```

## 查询条件

### 基本条件操作

```java
// 等于
Expressions.field("name").eq("张三")

// 不等于
Expressions.field("age").ne(18)

// 大于
Expressions.field("age").gt(18)

// 大于等于
Expressions.field("age").gte(18)

// 小于
Expressions.field("age").lt(30)

// 小于等于
Expressions.field("age").lte(30)

// 包含
Expressions.field("name").contains("张")

// 不包含
Expressions.field("name").notContains("李")

// 开始于
Expressions.field("name").startsWith("张")

// 结束于
Expressions.field("name").endsWith("三")

// 在列表中
Expressions.field("status").in("active", "pending")

// 不在列表中
Expressions.field("status").nin("inactive", "deleted")

// 在范围内
Expressions.field("age").between(18, 65)
```

### 复杂条件组合

```java
// AND 条件
Predicate condition = Expressions.field("age").gte(18)
    .and(Expressions.field("status").eq("active"))
    .and(Expressions.field("name").contains("张"));

// OR 条件
Predicate condition = Expressions.field("status").eq("active")
  .or(Expressions.field("status").eq("pending"));

// 使用复杂条件查询
List<User> users = session.dsl()
  .from(User.class)
  .where(condition)
  .execute();
```

### 字符串条件

```java
// 直接使用SQL条件字符串
List<User> users = session.dsl()
    .from(User.class)
    .where("age > 18 AND status = 'active'")
    .execute();
```

## 排序和分页

### 排序

```java
// 单字段排序
.orderBy("name", Session.Direction.ASC)
.orderBy("age", Session.Direction.DESC)

// 多字段排序
.orderBy("age", Session.Direction.DESC)
.orderBy("name", Session.Direction.ASC)
```

### 分页

```java
// 分页查询
.page(1, 10)  // 第1页，每页10条
.page(2, 5)   // 第2页，每页5条
```

## 分组和聚合

### 分组查询

```java
List<Map<String, Object>> result = session.dsl()
  .select("status", "count(*) as user_count")
  .from("users")
  .groupBy("status")
  .execute();
```

## 连接查询

### 内连接

```java
List<Map<String, Object>> result = session.dsl()
  .select("u.name", "u.email", "o.order_id")
  .from("users")
  .join(joins -> joins
    .addInnerJoin(join -> join
      .setFrom("orders")
      .setAs("o")
      .setLocalField("u.id")
      .setForeignField("o.user_id")
    )
  )
  .where(Expressions.field("u.status").eq("active"))
  .execute();
```

### 左连接

```java
List<Map<String, Object>> result = session.dsl()
  .select("u.name", "u.email", "o.order_id")
  .from("users")
  .join(joins -> joins
    .addLeftJoin(join -> join
      .setFrom("orders")
      .setAs("o")
      .setLocalField("u.id")
      .setForeignField("o.user_id")
    )
  )
  .where(Expressions.field("u.status").eq("active"))
  .execute();
```

## 执行查询

### 返回列表

```java
// 返回Map列表
List<Map<String, Object>> users = session.dsl()
    .from("users")
    .execute();

// 返回实体类列表
List<User> users = session.dsl()
  .from(User.class)
  .execute();
```

### 返回单个结果

```java
// 返回单个Map
Map<String, Object> user = session.dsl()
    .from("users")
    .where(Expressions.field("id").eq(1L))
    .executeOne();

// 返回单个实体
User user = session.dsl()
  .from(User.class)
  .where(Expressions.field("id").eq(1L))
  .executeOne(User.class);
```

### 统计查询

```java
// 统计记录数
long count = session.dsl()
    .from(User.class)
    .where(Expressions.field("age").gt(25))
    .count();

// 检查是否存在
boolean exists = session.dsl()
  .from(User.class)
  .where(Expressions.field("email").eq("test@example.com"))
  .exists();
```

## 嵌套查询

```java
// 启用嵌套查询
List<User> users = session.dsl()
    .from(User.class)
    .where(Expressions.field("status").eq("active"))
    .enableNested()
    .execute();
```

## 实体类定义

### 基本实体类

```java

@ModelClass("users")
public class User {

  @ModelField("id")
  private Long id;

  @ModelField("name")
  private String name;

  @ModelField("email")
  private String email;

  @ModelField("age")
  private Integer age;

  @ModelField("status")
  private String status;

  // Getter和Setter方法
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  // ... 其他getter和setter
}
```

## 完整示例

```java
public class UserService {

  private final Session session;

  public UserService(Session session) {
    this.session = session;
  }

  // 查询活跃用户
  public List<User> findActiveUsers() {
    return session.dsl()
      .from(User.class)
      .where(Expressions.field(User::getStatus).eq("active"))
      .orderBy("name", Session.Direction.ASC)
      .execute();
  }

  // 分页查询成年用户
  public List<User> findAdultUsers(int page, int size) {
    return session.dsl()
      .from(User.class)
      .where(Expressions.field(User::getAge).gte(18))
      .orderBy("age", Session.Direction.DESC)
      .page(page, size)
      .execute();
  }

  // 根据姓名模糊查询
  public List<User> findUsersByName(String name) {
    return session.dsl()
      .from(User.class)
      .where(Expressions.field(User::getName).contains(name))
      .execute();
  }

  // 统计用户数量
  public long countUsersByStatus(String status) {
    return session.dsl()
      .from(User.class)
      .where(Expressions.field(User::getStatus).eq(status))
      .count();
  }

  // 复杂条件查询
  public List<User> findUsersByComplexCondition() {
    Predicate condition = Expressions.field(User::getAge).gte(18)
      .and(Expressions.field(User::getStatus).eq("active"))
      .and(Expressions.field(User::getName).contains("张"));

    return session.dsl()
      .from(User.class)
      .where(condition)
      .orderBy("age", Session.Direction.DESC)
      .execute();
  }
}
```

## 优势

1. **类型安全**: 使用实体类和方法引用，编译时就能发现错误
2. **代码简洁**: 链式调用，代码更加简洁易读
3. **可复用**: 条件可以组合和复用
4. **灵活性**: 支持字符串条件和表达式条件
5. **功能完整**: 支持查询、排序、分页、分组、连接等所有功能

## 注意事项

1. 实体类需要使用 `@ModelClass` 和 `@ModelField` 注解
2. 方法引用需要遵循JavaBean规范（getXxx或isXxx）
3. 字符串条件需要手动确保SQL语法正确
4. 嵌套查询需要谨慎使用，避免N+1问题 
