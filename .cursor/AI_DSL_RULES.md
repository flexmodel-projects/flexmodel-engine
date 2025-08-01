# FlexModel DSL AI 使用规则

## 核心原则

### 1. 模型概念优先

- **正确**：使用 `model()` 方法指定模型名称
- **错误**：使用 `table()` 方法（不存在）
- **原因**：FlexModel基于模型概念，不是传统的关系型数据库表概念

### 2. 语法简洁性

- **优先使用**：`QueryDSL.simple()` 进行简单查询
- **复杂场景**：使用 `QueryDSL.query()` 进行复杂查询
- **避免**：直接使用原始的 `Query` 类构造方法

## 语法规范

### 查询构建器选择

#### 简单查询（推荐）

```java
// ✅ 正确：简单查询使用 simple()
QueryDSL.simple()
    .

select("id","name","age")
    .

where(QueryDSL.where("age").

gte(18))
  .

orderBy("name",Direction.ASC)
    .

limit(10)
    .

build();
```

#### 复杂查询

```java
// ✅ 正确：复杂查询使用 query()
QueryDSL.query()
    .

select(select ->select
  .

field("name")
        .

count("count","id")
    )
      .

innerJoin(join ->join.

model("orders"))
  .

groupBy(groupBy ->groupBy.

field("name"))
  .

build();
```

### 模型连接规范

#### 正确的模型连接语法

```java
// ✅ 正确：使用 model() 方法
.leftJoin(join ->join
  .

model("students")      // 指定模型名称
    .

as("students")         // 设置别名
    .

on("id","class_id")   // 指定关联字段
)
```

#### 错误的语法（避免使用）

```java
// ❌ 错误：使用 table() 方法
.leftJoin(join ->join
  .

table("students")      // 错误：不存在 table() 方法
    .

as("students")
    .

on("id","class_id")
)
```

### 条件表达式规范

#### 正确的条件构建

```java
// ✅ 正确：使用 QueryDSL.where() 构建条件
Predicate condition = QueryDSL.where("age").gte(18)
    .and(QueryDSL.where("status").eq("active"))
    .or(QueryDSL.where("vip").eq(true));
```

#### 错误的条件构建

```java
// ❌ 错误：直接使用 Expressions
Predicate condition = Expressions.field("age").gte(18)  // 不推荐
    .and(Expressions.field("status").eq("active"));
```

### 聚合函数规范

#### 正确的聚合函数使用

```java
// ✅ 正确：在 select 中直接使用
.select(select ->select
  .

count("total_count","id")
    .

sum("total_amount","amount")
    .

avg("avg_amount","amount")
)

// ✅ 正确：使用聚合构建器
  .

select(select ->select
  .

field("total_count",QueryDSL.agg().

count("id"))
  .

field("total_amount",QueryDSL.agg().

sum("amount"))
  )
```

### 日期函数规范

#### 正确的日期函数使用

```java
// ✅ 正确：在 select 中直接使用
.select(select ->select
  .

dateFormat("year","birthday","yyyy-MM-dd")
    .

field("day_of_week",QueryDSL.date().

dayOfWeek("birthday"))
  )

// ✅ 正确：使用日期构建器
  .

select(select ->select
  .

field("year",QueryDSL.date().

format("birthday","yyyy-MM-dd"))
  .

field("day_of_week",QueryDSL.date().

dayOfWeek("birthday"))
  )
```

## 常见错误模式

### 1. 概念混淆错误

#### 错误：使用表概念

```java
// ❌ 错误：使用表概念
Query query = new Query()
    .withJoin(joins -> {
      joins.addInnerJoin(join -> {
        join.setFrom("users_table");  // 错误：使用表名
        return join;
      });
      return joins;
    });
```

#### 正确：使用模型概念

```java
// ✅ 正确：使用模型概念
QueryDSL.query()
    .

innerJoin(join ->join
  .

model("users")  // 正确：使用模型名
    )
      .

build();
```

### 2. 语法冗长错误

#### 错误：使用原始语法

```java
// ❌ 错误：语法冗长
Query query = new Query()
    .withProjection(projection -> {
      projection.addField("user_id", new Query.QueryField("id"));
      projection.addField("user_name", new Query.QueryField("name"));
      return projection;
    })
    .withFilter(Expressions.field("age").gte(18));
```

#### 正确：使用优化语法

```java
// ✅ 正确：语法简洁
QueryDSL.simple()
    .

select("user_id","user_name")
    .

where(QueryDSL.where("age").

gte(18))
  .

build();
```

### 3. 方法调用错误

#### 错误：方法不存在

```java
// ❌ 错误：调用不存在的方法
QueryDSL.query()
    .

select(select ->select
  .

table("users")  // 错误：SelectBuilder 没有 table() 方法
    )
      .

build();
```

#### 正确：使用正确的方法

```java
// ✅ 正确：使用正确的方法
QueryDSL.query()
    .

select(select ->select
  .

field("user_id","id")  // 正确：使用 field() 方法
        .

field("user_name","name")
    )
      .

build();
```

## 最佳实践

### 1. 查询复杂度选择

#### 简单查询（推荐）

```java
// 适用于：单表查询、简单条件、基本排序分页
QueryDSL.simple()
    .

select("id","name","email")
    .

where(QueryDSL.where("status").

eq("active"))
  .

orderBy("name",Direction.ASC)
    .

limit(20)
    .

build();
```

#### 复杂查询

```java
// 适用于：多表连接、聚合函数、复杂条件
QueryDSL.query()
    .

select(select ->select
  .

field("department")
        .

count("employee_count","employees.id")
        .

avg("avg_salary","employees.salary")
    )
      .

innerJoin(join ->join
  .

model("employees")
        .

where(QueryDSL.where("status").

eq("active"))
  )
  .

groupBy(groupBy ->groupBy.

field("department"))
  .

orderBy(orderBy ->orderBy.

desc("avg_salary"))
  .

build();
```

### 2. 命名规范

#### 模型命名

```java
// ✅ 正确：使用业务模型名称
.model("users")           // 用户模型
.

model("orders")          // 订单模型
.

model("products")        // 产品模型
.

model("user_profiles")   // 用户档案模型
```

#### 字段命名

```java
// ✅ 正确：使用有意义的别名
.select(select ->select
  .

field("user_id","id")           // 明确字段含义
    .

field("user_name","name")       // 避免歧义
    .

field("user_email","email")     // 清晰标识
)
```

### 3. 条件构建规范

#### 复杂条件组织

```java
// ✅ 正确：分步构建复杂条件
Predicate ageCondition = QueryDSL.where("age").gte(18)
    .and(QueryDSL.where("age").lte(65));

Predicate statusCondition = QueryDSL.where("status").eq("active")
  .or(QueryDSL.where("vip").eq(true));

Predicate finalCondition = ageCondition.and(statusCondition);

QueryDSL.

simple()
    .

select("id","name","age","status")
    .

where(finalCondition)
    .

build();
```

## 性能优化建议

### 1. 字段选择优化

```java
// ✅ 正确：只选择需要的字段
QueryDSL.simple()
    .

select("id","name")  // 只选择必要字段
    .

where(QueryDSL.where("status").

eq("active"))
  .

build();

// ❌ 错误：选择所有字段
QueryDSL.

simple()
    .

select("*")  // 避免选择所有字段
    .

where(QueryDSL.where("status").

eq("active"))
  .

build();
```

### 2. 分页优化

```java
// ✅ 正确：合理设置分页大小
QueryDSL.simple()
    .

select("id","name")
    .

where(QueryDSL.where("status").

eq("active"))
  .

page(1,20)  // 合理的分页大小
    .

build();
```

## 调试和错误处理

### 1. 语法验证

```java
// 验证查询构建是否成功
try{
Query query = QueryDSL.simple()
  .select("id", "name")
  .where(QueryDSL.where("age").gte(18))
  .build();
    System.out.

println("查询构建成功");
}catch(
Exception e){
  System.err.

println("查询构建失败: "+e.getMessage());
  }
```

### 2. 常见错误排查

- **编译错误**：检查方法名是否正确（如 `model()` 而不是 `table()`）
- **运行时错误**：检查模型名称是否存在
- **逻辑错误**：检查条件表达式是否正确

## 总结

1. **始终使用模型概念**：使用 `model()` 而不是 `table()`
2. **选择合适的构建器**：简单查询用 `simple()`，复杂查询用 `query()`
3. **使用优化的语法**：避免直接使用原始的 `Query` 类
4. **遵循命名规范**：使用有意义的模型和字段名称
5. **注意性能优化**：只选择必要字段，合理设置分页

这些规则将帮助AI正确理解和使用FlexModel的DSL语法，避免常见错误，提高代码质量和开发效率。 
