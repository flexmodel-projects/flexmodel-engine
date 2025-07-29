# FlexModel GraphQL Module

[![GraphQL](https://img.shields.io/badge/GraphQL-22.3+-purple.svg)](https://graphql.org/)
[![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://openjdk.java.net/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/)

> FlexModel GraphQL模块为FlexModel引擎提供完整的GraphQL支持，包括查询、变更、订阅和类型系统。

## 🚀 特性

### 核心功能
- **自动Schema生成** - 根据数据模型自动生成GraphQL Schema
- **类型映射** - 完整的Java类型到GraphQL类型映射
- **查询优化** - 智能查询优化和N+1问题解决
- **权限控制** - 内置权限验证和访问控制
- **实时订阅** - 支持GraphQL Subscription

### 高级特性
- **自定义标量** - 支持自定义GraphQL标量类型
- **指令支持** - 支持GraphQL指令扩展
- **批量操作** - 支持批量查询和变更
- **缓存集成** - 与FlexModel缓存系统集成

## 📦 安装

### Maven依赖

```xml
<dependency>
    <groupId>tech.wetech.flexmodel</groupId>
    <artifactId>flexmodel-graphql</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## 🚀 快速开始

### 1. 基本配置

```java
// 创建GraphQL提供者
GraphQLProvider graphQLProvider = GraphQLProvider.builder()
    .sessionFactory(sessionFactory)
    .build();

// 获取GraphQL实例
GraphQL graphQL = graphQLProvider.getGraphQL();
```

### 2. 执行查询

```java
// 执行GraphQL查询
ExecutionInput input = ExecutionInput.newExecutionInput()
    .query("query { users { id name email } }")
    .build();

ExecutionResult result = graphQL.execute(input);
```

### 3. 执行变更

```java
// 执行GraphQL变更
String mutation = """
    mutation CreateUser($input: UserInput!) {
        createUser(input: $input) {
            id
            name
            email
        }
    }
    """;

Map<String, Object> variables = Map.of(
    "input", Map.of(
        "name", "John Doe",
        "email", "john@example.com"
    )
);

ExecutionInput input = ExecutionInput.newExecutionInput()
    .query(mutation)
    .variables(variables)
    .build();

ExecutionResult result = graphQL.execute(input);
```

## 📖 使用指南

### 1. Schema自动生成

GraphQL Schema会根据你的数据模型自动生成：

```graphql
type User {
  id: ID!
  name: String!
  email: String!
  createdAt: DateTime!
  updatedAt: DateTime!
}

type Query {
  users: [User!]!
  user(id: ID!): User
  usersByEmail(email: String!): [User!]!
}

type Mutation {
  createUser(input: UserInput!): User!
  updateUser(id: ID!, input: UserInput!): User!
  deleteUser(id: ID!): Boolean!
}

input UserInput {
  name: String!
  email: String!
}
```

### 2. 类型映射

| Java类型 | GraphQL类型 | 说明 |
|----------|-------------|------|
| String | String | 字符串类型 |
| Integer | Int | 整数类型 |
| Long | Long | 长整数类型 |
| Double | Float | 浮点数类型 |
| Boolean | Boolean | 布尔类型 |
| LocalDateTime | DateTime | 日期时间类型 |
| LocalDate | Date | 日期类型 |
| Enum | Enum | 枚举类型 |

### 3. 自定义标量

```java
// 定义自定义标量
public class JsonScalar extends GraphQLScalarType {
    public JsonScalar() {
        super("JSON", "JSON scalar type", new Coercing<Object, Object>() {
            @Override
            public Object serialize(Object dataFetcherResult) {
                return dataFetcherResult;
            }

            @Override
            public Object parseValue(Object input) {
                return input;
            }

            @Override
            public Object parseLiteral(Object input) {
                return input;
            }
        });
    }
}

// 注册自定义标量
GraphQLProvider graphQLProvider = GraphQLProvider.builder()
    .sessionFactory(sessionFactory)
    .addScalar(new JsonScalar())
    .build();
```

### 4. 权限控制

```java
// 定义权限注解
@GraphQLField(requiresPermission = "user:read")
public List<User> getUsers() {
    return userService.findAll();
}

@GraphQLField(requiresPermission = "user:write")
public User createUser(@GraphQLArgument("input") UserInput input) {
    return userService.create(input);
}
```

### 5. 数据加载器

```java
// 定义数据加载器
DataLoader<Long, User> userLoader = DataLoader.newDataLoader(userIds -> 
    CompletableFuture.supplyAsync(() -> userService.findByIds(userIds))
);

// 在查询中使用
ExecutionInput input = ExecutionInput.newExecutionInput()
    .query("query { users { id name posts { id title author { name } } } }")
    .dataLoaderRegistry(DataLoaderRegistry.newRegistry().register("user", userLoader))
    .build();
```

## 🔧 配置选项

### 1. 性能配置

```java
GraphQLProvider graphQLProvider = GraphQLProvider.builder()
    .sessionFactory(sessionFactory)
    .maxQueryDepth(10)
    .maxQueryComplexity(1000)
    .maxQueryCost(100)
    .build();
```

### 2. 缓存配置

```java
GraphQLProvider graphQLProvider = GraphQLProvider.builder()
    .sessionFactory(sessionFactory)
    .enableQueryCache(true)
    .queryCacheSize(1000)
    .build();
```

### 3. 错误处理

```java
GraphQLProvider graphQLProvider = GraphQLProvider.builder()
    .sessionFactory(sessionFactory)
    .errorHandler(new CustomErrorHandler())
    .build();
```

## 🧪 测试

### 单元测试

```java
@Test
public void testUserQuery() {
    // 准备测试数据
    User user = new User();
    user.setId(1L);
    user.setName("John Doe");
    user.setEmail("john@example.com");
    
    // 执行查询
    String query = "query { user(id: 1) { id name email } }";
    ExecutionInput input = ExecutionInput.newExecutionInput()
        .query(query)
        .build();
    
    ExecutionResult result = graphQL.execute(input);
    
    // 验证结果
    assertFalse(result.getErrors().isEmpty());
    Map<String, Object> data = result.getData();
    // 验证数据...
}
```

### 集成测试

```java
@SpringBootTest
@AutoConfigureTestDatabase
class GraphQLIntegrationTest {
    
    @Autowired
    private GraphQL graphQL;
    
    @Test
    void testCreateUser() {
        String mutation = """
            mutation CreateUser($input: UserInput!) {
                createUser(input: $input) {
                    id
                    name
                    email
                }
            }
            """;
        
        Map<String, Object> variables = Map.of(
            "input", Map.of(
                "name", "Jane Doe",
                "email", "jane@example.com"
            )
        );
        
        ExecutionInput input = ExecutionInput.newExecutionInput()
            .query(mutation)
            .variables(variables)
            .build();
        
        ExecutionResult result = graphQL.execute(input);
        assertTrue(result.getErrors().isEmpty());
    }
}
```

## 📊 性能优化

### 1. 查询优化

- **字段选择优化**: 只查询需要的字段
- **批量加载**: 使用DataLoader避免N+1问题
- **查询缓存**: 缓存常用查询结果

### 2. 监控指标

```java
// 启用性能监控
GraphQLProvider graphQLProvider = GraphQLProvider.builder()
    .sessionFactory(sessionFactory)
    .enableMetrics(true)
    .build();

// 获取性能指标
GraphQLMetrics metrics = graphQLProvider.getMetrics();
System.out.println("查询执行时间: " + metrics.getAverageQueryTime());
System.out.println("缓存命中率: " + metrics.getCacheHitRate());
```

## 🔒 安全性

### 1. 查询限制

```java
GraphQLProvider graphQLProvider = GraphQLProvider.builder()
    .sessionFactory(sessionFactory)
    .maxQueryDepth(10)           // 最大查询深度
    .maxQueryComplexity(1000)    // 最大查询复杂度
    .maxQueryCost(100)           // 最大查询成本
    .build();
```

### 2. 权限验证

```java
// 自定义权限验证器
public class CustomPermissionValidator implements PermissionValidator {
    @Override
    public boolean hasPermission(String permission, Object context) {
        // 实现权限验证逻辑
        return true;
    }
}

GraphQLProvider graphQLProvider = GraphQLProvider.builder()
    .sessionFactory(sessionFactory)
    .permissionValidator(new CustomPermissionValidator())
    .build();
```

## 📚 示例项目

查看完整的示例项目：[flexmodel-graphql-example](https://github.com/flexmodel-projects/flexmodel-graphql-example)

### 示例特性

- 完整的CRUD操作
- 权限控制示例
- 自定义标量类型
- 数据加载器使用
- 性能优化示例

## 🤝 贡献

我们欢迎所有形式的贡献！请查看我们的[贡献指南](../../CONTRIBUTING.md)。

### 开发环境设置

1. Fork项目
2. 克隆你的fork
3. 创建特性分支
4. 提交更改
5. 推送到分支
6. 创建Pull Request

## 📄 许可证

本项目采用 [Apache License 2.0](../../LICENSE) 许可证。

## 🆘 支持

### 获取帮助

- 📖 [GraphQL文档](https://graphql.org/learn/)
- 💬 [讨论区](https://github.com/flexmodel-projects/flexmodel-engine/discussions)
- 🐛 [问题报告](https://github.com/flexmodel-projects/flexmodel-engine/issues)

---

**FlexModel GraphQL** - 让GraphQL开发更简单、更高效！
