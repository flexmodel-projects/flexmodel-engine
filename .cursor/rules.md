# FlexModel DSL Cursor 规则

## 概述

这是FlexModel项目的Cursor AI规则文件，用于指导AI助手正确使用FlexModel的DSL语法。

## 核心原则

### 1. 模型概念优先

- **正确**：使用 `model()` 方法指定模型名称
- **错误**：使用 `table()` 方法（不存在）
- **原因**：FlexModel基于模型概念，不是传统的关系型数据库表概念

### 2. 语法简洁性

- **优先使用**：`QueryBuilder.create()` 进行简单查询
- **复杂场景**：使用 `QueryBuilder.create()` 进行复杂查询
- **避免**：直接使用原始的 `Query` 类构造方法

## 语法规范

### 查询构建器选择

#### 简单查询（推荐）

```java
// ✅ 正确：简单查询使用 create()
QueryBuilder.create()
    .select("id","name","age")
    .where(Expressions.field("age").gte(18))
    .orderBy(orderBy -> orderBy.asc("name"))
    .page(1, 10)
    .build();
```

#### 复杂查询

```java
// ✅ 正确：复杂查询使用 create()
QueryBuilder.create()
    .select(select -> select
        .field("name")
        .count("count", "id")
    )
    .innerJoin(join -> join
        .model("orders")
    )
    .groupBy(groupBy -> groupBy
        .field("name")
    )
    .build();
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
// ✅ 正确：使用 Expressions.field() 构建条件
Predicate condition = Expressions.field("age").gte(18)
    .and(Expressions.field("status").eq("active"))
    .or(Expressions.field("vip").eq(true));
```

#### 错误的条件构建

```java
// ❌ 错误：直接使用 Expressions
Predicate condition = Expressions.field("age").gte(18)  // 不推荐
    .and(Expressions.field("status").eq("active"));
```

## 常见错误模式

### 1. 概念混淆错误

- **错误**：使用表概念而不是模型概念
- **修复**：将表名改为模型名，使用model()方法

### 2. 语法冗长错误

- **错误**：使用原始语法而不是优化的DSL语法
- **修复**：使用QueryDSL.simple()或QueryDSL.query()替代原始Query类

### 3. 方法调用错误

- **错误**：调用不存在的方法
- **修复**：检查方法名是否正确，SelectBuilder使用field()方法

## 最佳实践

### 1. 查询复杂度选择

- **简单查询**：适用于单表查询、简单条件、基本排序分页
- **复杂查询**：适用于多表连接、聚合函数、复杂条件

### 2. 命名规范

- **模型命名**：使用业务模型名称，避免使用表名
- **字段命名**：使用有意义的别名，避免歧义

### 3. 性能优化

- **字段选择**：只选择需要的字段，避免选择所有字段
- **分页优化**：合理设置分页大小

## 代码示例

### 基础查询

```java
QueryBuilder.create()
    .select("id","name","email")
    .where(Expressions.field("status").eq("active"))
    .build();
```

### 连接查询

```java
QueryBuilder.create()
    .select(select -> select
        .field("user_name","users.name")
        .field("order_count","orders.id")
    )
    .leftJoin(join -> join
        .model("orders")
        .on("users.id","orders.user_id")
    )
    .build();
```

### 聚合查询

```java
QueryBuilder.create()
    .select(select -> select
        .field("department")
        .count("employee_count","employees.id")
        .avg("avg_salary","employees.salary")
    )
    .groupBy(groupBy -> groupBy
        .field("department")
    )
    .build();
```

## 错误检查清单

在提供代码示例前，请检查：

- [ ] 是否使用了 `model()` 而不是 `table()`
- [ ] 是否选择了合适的查询构建器（simple() vs query()）
- [ ] 方法名是否正确
- [ ] 语法结构是否完整
- [ ] 是否提供了必要的解释

## 响应格式

当回答FlexModel DSL相关问题时，请：

1. 提供完整的代码示例
2. 解释每个方法的作用
3. 说明注意事项
4. 提供最佳实践建议
5. 包含错误预防提示

## 相关文件

- `AI_DSL_RULES.md` - 详细的AI使用规则
- `ai_dsl_config.json` - JSON格式的配置
- `ai_dsl_rules.yaml` - YAML格式的配置
- `ai_prompt_template.txt` - AI提示模板

这些规则将帮助Cursor AI正确理解和使用FlexModel的DSL语法，避免常见错误，提高代码质量和开发效率。 
