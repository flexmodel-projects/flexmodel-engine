---
sidebar_position: 20
---

# 查询条件

Flexmodel 支持灵活的查询条件语法，用于过滤和查询数据记录。

## 基础语法

查询条件使用 JSON 格式，支持以下操作符：

> **快捷等于写法**：对于最常见的等值过滤，可以直接写成 `{"字段名": 值}`，系统会自动转换为 `{"字段名": {"_eq": 值}}`。

### 比较操作符

| 操作符 | 描述 | 示例 |
|--------|------|------|
| `_eq` | 等于 | `{"name": {"_eq": "张三"}}` |
| `_ne` | 不等于 | `{"age": {"_ne": 18}}` |
| `_gt` | 大于 | `{"age": {"_gt": 18}}` |
| `_gte` | 大于等于 | `{"age": {"_gte": 18}}` |
| `_lt` | 小于 | `{"age": {"_lt": 65}}` |
| `_lte` | 小于等于 | `{"age": {"_lte": 65}}` |

### 字符串操作符

| 操作符 | 描述 | 示例 |
|--------|------|------|
| `_contains` | 包含 | `{"name": {"_contains": "张"}}` |
| `_not_contains` | 不包含 | `{"name": {"_not_contains": "李"}}` |
| `_starts_with` | 以...开始 | `{"name": {"_starts_with": "张"}}` |
| `_ends_with` | 以...结束 | `{"name": {"_ends_with": "三"}}` |

### 集合操作符

| 操作符 | 描述 | 示例 |
|--------|------|------|
| `_in` | 在...中 | `{"status": {"_in": ["active", "pending"]}}` |
| `_nin` | 不在...中 | `{"status": {"_nin": ["deleted"]}}` |

### 范围操作符

| 操作符 | 描述 | 示例 |
|--------|------|------|
| `_between` | 在...之间 | `{"age": {"_between": [18, 65]}}` |

### 逻辑操作符

| 操作符 | 描述 | 示例 |
|--------|------|------|
| `_and` | 与 | `{"_and": [{"age": {"_gte": 18}}, {"status": {"_eq": "active"}}]}` |
| `_or` | 或 | `{"_or": [{"status": {"_eq": "active"}}, {"status": {"_eq": "pending"}}]}` |

## 查询示例

### 单个条件

```json
{
  "username": {
    "_eq": "john_doe"
  }
}
```

### 多个条件（AND）

```json
{
  "_and": [
    {
      "username": {
        "_eq": "john_doe"
      }
    },
    {
      "age": {
        "_gte": 18
      }
    },
    {
      "status": {
        "_eq": "active"
      }
    }
  ]
}
```

### 多个条件（OR）

```json
{
  "_or": [
    {
      "status": {
        "_eq": "active"
      }
    },
    {
      "status": {
        "_eq": "pending"
      }
    }
  ]
}
```

### 复杂嵌套条件

```json
{
  "_and": [
    {
      "age": {
        "_gte": 18
      }
    },
    {
      "_or": [
        {
          "gender": {
            "_eq": "MALE"
          }
        },
        {
          "gender": {
            "_eq": "FEMALE"
          }
        }
      ]
    },
    {
      "createdAt": {
        "_between": [
          "2023-01-01 00:00:00",
          "2023-12-31 23:59:59"
        ]
      }
    }
  ]
}
```

### 字符串搜索

```json
{
  "_or": [
    {
      "name": {
        "_contains": "张"
      }
    },
    {
      "email": {
        "_contains": "zhang"
      }
    }
  ]
}
```

### 枚举值查询

```json
{
  "status": {
    "_in": ["ACTIVE", "PENDING", "APPROVED"]
  }
}
```

### 日期时间查询

```json
{
  "_and": [
    {
      "createdAt": {
        "_gte": "2023-01-01 00:00:00"
      }
    },
    {
      "updatedAt": {
        "_lte": "2023-12-31 23:59:59"
      }
    }
  ]
}
```

## 在 API 中使用

### REST API

在查询记录时，通过 `filter` 参数传递查询条件：

```bash
GET /api/v1/datasources/{datasourceName}/models/{modelName}/records?filter={"status":{"_eq":"active"}}
```

### GraphQL API

在 GraphQL 查询中使用 `where` 参数：

```graphql
query {
  students(where: {age: {_gte: 18}}) {
    id
    name
    age
  }
}
```

## 注意事项

1. **数据类型匹配**: 确保查询条件的值与字段类型匹配
2. **日期格式**: 日期时间字段使用 ISO 8601 格式：`YYYY-MM-DD HH:mm:ss`
3. **字符串转义**: 在 URL 中传递 JSON 时需要正确转义
4. **性能考虑**: 复杂查询可能影响性能，建议在相关字段上创建索引
5. **大小写敏感**: 字符串比较默认区分大小写
6. **渲染机制**: Flexmodel-Core 会先将 DSL 解析为内部条件语法树，再分别渲染为 SQL 或 Mongo 查询语句

## 高级用法

### 关联查询

查询条件支持关联字段：

```json
{
  "studentClass": {
    "className": {
      "_contains": "计算机"
    }
  }
}
```

### 嵌套查询

支持多层嵌套的关联查询：

```json
{
  "studentClass": {
    "teacher": {
      "name": {
        "_contains": "李"
      }
    }
  }
}
```

### 空值查询

查询空值或非空值：

```json
{
  "_or": [
    {
      "description": {
        "_eq": null
      }
    },
    {
      "description": {
        "_ne": null
      }
    }
  ]
}
```

