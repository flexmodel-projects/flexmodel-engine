# FlexModel DSL 实现总结

## 实现概述

我们成功实现了一个功能完整的DSL（领域特定语言）来简化FlexModel的数据操作。这个DSL提供了类似SQL的语法，同时保持了类型安全和代码简洁性。

## 核心功能

### 1. DSL查询构建器

在 `Session` 接口中添加了 `dsl()` 方法，返回 `DSLQueryBuilder` 实例：

```java
DSLQueryBuilder dsl = session.dsl();
```

### 2. 链式调用语法

支持流畅的链式调用，代码简洁易读：

```java
List<User> users = session.dsl()
  .from(User.class)
  .execute();
```

### 3. 类型安全的字段引用

扩展了 `Expressions` 类，支持方法引用：

```java
// 字符串字段
Expressions.field("name").

eq("张三")

// 方法引用（类型安全）
Expressions.

field(User::getName).

eq("张三")
```

### 4. 完整的查询功能

- **基本查询**: `select()`, `from()`, `where()`
- **排序**: `orderBy()`
- **分页**: `page()`, `limit()`, `offset()`
- **分组**: `groupBy()`
- **连接**: `join()`, `leftJoin()`
- **嵌套查询**: `enableNested()`

### 5. 条件表达式

支持丰富的条件操作：

```java
// 基本比较
.eq(), .

ne(), .

gt(), .

gte(), .

lt(), .

lte()

// 字符串操作
.

contains(), .

notContains(), .

startsWith(), .

endsWith()

// 集合操作
.

in(), .

nin(), .

between()

// 逻辑组合
.

and(), .

or()
```

## 实现细节

### 1. Session接口扩展

在 `Session` 接口中添加了：

- `dsl()` 方法：创建DSL查询构建器
- `DSLQueryBuilder` 内部类：实现查询构建逻辑
- `Direction` 枚举：定义排序方向

### 2. Expressions类扩展

扩展了 `Expressions` 类：

- 添加了支持方法引用的 `field()` 方法
- 实现了字段名提取逻辑
- 保持了与现有 `Predicate` 系统的兼容性

### 3. 实体类支持

- 支持通过 `@ModelClass` 注解映射实体类到表名
- 支持通过 `@ModelField` 注解映射字段
- 自动从实体类获取模型名称

## 使用示例

### 基本查询

```java
// 查询所有用户
List<User> allUsers = session.dsl()
    .from(User.class)
    .execute();

// 条件查询
List<User> activeUsers = session.dsl()
  .from(User.class)
  .where(Expressions.field(User::getStatus).eq("active"))
  .execute();
```

### 复杂查询

```java
// 复杂条件
Predicate condition = Expressions.field(User::getAge).gte(18)
    .and(Expressions.field(User::getStatus).eq("active"))
    .and(Expressions.field(User::getName).contains("张"));

List<User> users = session.dsl()
  .from(User.class)
  .where(condition)
  .orderBy("age", Session.Direction.DESC)
  .page(1, 10)
  .execute();
```

### 连接查询

```java
List<Map<String, Object>> result = session.dsl()
  .select("u.name", "u.email", "o.order_id")
  .from("users")
  .leftJoin(joins -> joins
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

### 统计查询

```java
// 统计记录数
long count = session.dsl()
    .from(User.class)
    .where(Expressions.field(User::getAge).gt(25))
    .count();

// 检查存在性
boolean exists = session.dsl()
  .from(User.class)
  .where(Expressions.field(User::getEmail).eq("test@example.com"))
  .exists();
```

## 优势

### 1. 类型安全

- 使用实体类和方法引用，编译时就能发现错误
- 避免了字符串字段名的拼写错误

### 2. 代码简洁

- 链式调用，代码更加简洁易读
- 减少了样板代码

### 3. 可复用性

- 条件可以组合和复用
- 查询构建器可以重复使用

### 4. 灵活性

- 支持字符串条件和表达式条件
- 可以混合使用不同的查询方式

### 5. 功能完整

- 支持查询、排序、分页、分组、连接等所有功能
- 与现有的Query系统完全兼容

## 与现有系统的集成

### 1. 兼容性

- 完全兼容现有的 `Query` 类
- 复用现有的 `Predicate` 系统
- 保持与 `DataOperations` 接口的一致性

### 2. 扩展性

- 可以轻松添加新的查询方法
- 支持自定义条件表达式
- 可以扩展支持更多数据库特性

## 文件结构

```
flexmodel-core/src/main/java/tech/wetech/flexmodel/
├── session/
│   └── Session.java                    # 添加了DSL功能
├── query/expr/
│   ├── Expressions.java                # 扩展了方法引用支持
│   ├── Predicate.java                  # 现有条件系统
│   └── FilterExpression.java           # 现有字段表达式
├── example/
│   ├── User.java                       # 示例实体类
│   ├── DSLExample.java                 # 基本使用示例
│   ├── AdvancedDSLExample.java         # 高级使用示例
│   ├── DSL_README.md                   # 使用指南
│   └── DSL_IMPLEMENTATION_SUMMARY.md   # 实现总结
└── test/
    └── DSLTest.java                    # 功能测试
```

## 总结

我们成功实现了一个功能完整、类型安全、易于使用的DSL系统，它：

1. **简化了数据操作**：提供了类似SQL的简洁语法
2. **保持了类型安全**：支持实体类和方法引用
3. **提供了完整功能**：支持所有常见的查询操作
4. **保持了兼容性**：与现有系统完全兼容
5. **具有良好的扩展性**：可以轻松添加新功能

这个DSL系统大大提升了FlexModel的易用性，让开发者可以更高效地进行数据操作，同时保持了代码的可读性和可维护性。 
