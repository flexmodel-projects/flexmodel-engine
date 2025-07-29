# FlexModel Engine

[![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://openjdk.java.net/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](LICENSE)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)](https://github.com/flexmodel-projects/flexmodel-engine)

> FlexModel 是一个开源的、免费的统一数据访问层解决方案，专为下一代应用程序设计。它提供全面的数据建模、API管理和数据源集成功能，支持私有化部署。

## 🚀 特性

### 核心功能
- **统一数据访问层** - 支持多种数据库的统一访问接口
- **动态数据建模** - 运行时动态创建和修改数据模型
- **智能代码生成** - 自动生成DAO、DSL、Entity等代码
- **GraphQL支持** - 内置GraphQL查询和变更支持
- **JSON逻辑引擎** - 强大的JSON表达式计算引擎

### 数据库支持
- **关系型数据库**: MySQL, PostgreSQL, Oracle, SQL Server, SQLite, MariaDB, TiDB, DB2, DM, GBase
- **NoSQL数据库**: MongoDB
- **扩展性**: 支持自定义数据库方言

### 高级特性
- **多数据源管理** - 支持同时连接多个数据源
- **缓存机制** - 内置多级缓存支持
- **事务管理** - 完整的事务支持
- **类型安全** - 强类型的数据访问
- **性能优化** - 查询优化和连接池管理

## 📋 目录

- [快速开始](#快速开始)
- [架构设计](#架构设计)
- [核心模块](#核心模块)
- [使用指南](#使用指南)
- [API文档](#api文档)
- [示例项目](#示例项目)
- [贡献指南](#贡献指南)
- [许可证](#许可证)

## 🏗️ 架构设计

```
┌─────────────────────────────────────────────────────────────┐
│                    FlexModel Engine                         │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │   GraphQL   │  │ JSON Logic  │  │   CodeGen   │         │
│  │   Module    │  │   Engine    │  │   Module    │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
├─────────────────────────────────────────────────────────────┤
│                    Core Engine                              │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │   Parser    │  │   Mapping   │  │   Session   │         │
│  │   Module    │  │   Module    │  │   Module    │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
├─────────────────────────────────────────────────────────────┤
│                    Data Access Layer                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │     SQL     │  │   MongoDB   │  │   Custom    │         │
│  │   Dialect   │  │   Support   │  │   Dialect   │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
├─────────────────────────────────────────────────────────────┤
│                    Data Sources                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │   MySQL     │  │ PostgreSQL  │  │   Oracle    │         │
│  │ PostgreSQL  │  │   MongoDB   │  │   SQLite    │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
└─────────────────────────────────────────────────────────────┘
```

## 🔧 核心模块

### flexmodel-core
核心引擎模块，提供基础的数据访问和模型管理功能。

### flexmodel-codegen
代码生成模块，自动生成DAO、DSL、Entity等代码。

### flexmodel-graphql
GraphQL支持模块，提供GraphQL查询和变更功能。

### flexmodel-json-logic
JSON逻辑引擎，支持复杂的JSON表达式计算。

### flexmodel-maven-plugin
Maven插件，集成到Maven构建流程中。

## 🚀 快速开始

### 环境要求
- Java 21+
- Maven 3.6+
- 支持的数据库（MySQL、PostgreSQL等）

### Maven依赖

```xml
<dependency>
    <groupId>tech.wetech.flexmodel</groupId>
    <artifactId>flexmodel-core</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 基本使用

```java
// 创建SessionFactory
SessionFactory sessionFactory = SessionFactory.builder()
    .setDefaultDataSourceProvider(new JdbcDataSourceProvider(dataSource))
    .setCache(new ConcurrentHashMapCache())
    .build();

// 创建Session
try (Session session = sessionFactory.createSession("mySchema")) {
    // 加载模型定义
    sessionFactory.loadJSONString("mySchema", jsonSchema);
    
    // 执行数据操作
    DataOperations operations = session.getDataOperations();
    List<Map<String, Object>> results = operations.query("SELECT * FROM users");
}
```

## 📖 使用指南

### 1. 数据模型定义

使用JSON格式定义数据模型：

```json
{
  "schema": [
    {
      "type": "ENTITY",
      "name": "User",
      "comment": "用户表",
      "fields": [
        {
          "type": "INT",
          "name": "id",
          "identity": true,
          "autoIncrement": true
        },
        {
          "type": "STRING",
          "name": "name",
          "length": 100,
          "nullable": false
        },
        {
          "type": "STRING",
          "name": "email",
          "length": 255,
          "unique": true
        }
      ]
    }
  ]
}
```

### 2. GraphQL查询

```graphql
query {
  users {
    id
    name
    email
  }
}
```

### 3. JSON逻辑表达式

```json
{
  "and": [
    { ">": [{"var": "age"}, 18] },
    { "==": [{"var": "status"}, "active"] }
  ]
}
```

### 4. 代码生成

```java
// 使用Maven插件生成代码
<plugin>
    <groupId>tech.wetech.flexmodel</groupId>
    <artifactId>flexmodel-maven-plugin</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## 🔌 数据库支持

### 已支持的数据库

| 数据库 | 版本 | 状态 | 特性支持 |
|--------|------|------|----------|
| MySQL | 5.7+ | ✅ | 完整支持 |
| PostgreSQL | 9.6+ | ✅ | 完整支持 |
| Oracle | 11g+ | ✅ | 完整支持 |
| SQL Server | 2012+ | ✅ | 完整支持 |
| SQLite | 3.x | ✅ | 完整支持 |
| MariaDB | 10.x | ✅ | 完整支持 |
| TiDB | 5.x | ✅ | 完整支持 |
| MongoDB | 4.x+ | ✅ | 完整支持 |
| DB2 | 11.x | ✅ | 基础支持 |
| DM | 8.x | ✅ | 基础支持 |
| GBase | 8.x | ✅ | 基础支持 |

### 自定义方言

```java
public class CustomSqlDialect extends AbstractSqlDialect {
    @Override
    public String getTypeName(int code, long length, int precision, int scale) {
        // 自定义类型映射
        return super.getTypeName(code, length, precision, scale);
    }
}
```

## 📊 性能优化

### 缓存策略
- **模型缓存**: 缓存数据模型定义
- **查询缓存**: 缓存常用查询结果
- **连接池**: 数据库连接池管理

### 查询优化
- **索引建议**: 自动分析查询并建议索引
- **查询计划**: SQL查询计划分析
- **批量操作**: 支持批量数据处理

## 🔒 安全性

### 数据安全
- **SQL注入防护**: 参数化查询
- **权限控制**: 细粒度权限管理
- **数据脱敏**: 敏感数据脱敏支持

### 访问控制
- **认证机制**: 支持多种认证方式
- **审计日志**: 操作审计日志记录

## 🧪 测试

### 运行测试

```bash
# 运行所有测试
mvn test

# 运行特定模块测试
mvn test -pl flexmodel-core

# 运行集成测试
mvn test -pl integration-tests
```

### 测试覆盖

项目包含完整的测试套件：
- 单元测试
- 集成测试
- 性能测试
- 兼容性测试

## 📈 监控和日志

### 性能监控
- **查询性能**: 查询执行时间统计
- **连接池**: 连接池使用情况监控
- **缓存命中率**: 缓存性能指标

### 日志配置

```properties
# log4j.properties
log4j.logger.tech.wetech.flexmodel=DEBUG
log4j.logger.tech.wetech.flexmodel.sql=INFO
```

## 🤝 贡献指南

我们欢迎所有形式的贡献！请查看我们的[贡献指南](CONTRIBUTING.md)。

### 开发环境设置

1. Fork项目
2. 克隆你的fork
3. 创建特性分支
4. 提交更改
5. 推送到分支
6. 创建Pull Request

### 代码规范

- 遵循Java代码规范
- 添加适当的注释和文档
- 编写单元测试
- 确保所有测试通过

## 📄 许可证

本项目采用 [Apache License 2.0](LICENSE) 许可证。

## 🆘 支持

### 获取帮助

- 📖 [文档](https://flexmodel.io/docs)
- 💬 [讨论区](https://github.com/flexmodel-projects/flexmodel-engine/discussions)
- 🐛 [问题报告](https://github.com/flexmodel-projects/flexmodel-engine/issues)
- 📧 [邮件支持](mailto:support@flexmodel.io)

### 社区

- 🌐 [官网](https://flexmodel.io)
- 📺 [YouTube](https://youtube.com/@flexmodel)
- 🐦 [Twitter](https://twitter.com/flexmodel_io)

## 🙏 致谢

感谢所有为FlexModel项目做出贡献的开发者和用户！

---

**FlexModel Engine** - 让数据访问更简单、更高效、更灵活！
