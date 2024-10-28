# FlexModel

功能丰富、操作灵活、可定制的企业级低代码建模引擎

## 主要特性一览

* 提供开发者友好的API，统一封装，屏蔽数据库差异，一招吃遍天下

* 支持数据库表、视图、字段、序列、索引的动态创建和删除，保证足够灵活

* 支持多达十几种数据库的适配，且已经拥有大量集成测试用例覆盖，以确保适用性和可靠性

* 支持自定义业务字段类型、数据验证器、值计算逻辑、建模的持久化方式

## 已经支持的数据库

以下为已经适配，集成测试用例测试通过的数据库版本

**关系型数据库**

| 数据库名称      | 兼容版本/已验证版本    | 连接参数                   |
|------------|---------------|------------------------|
| MySQL      | 8.0           |                        |
| MariaDB    | 10.3.6        |                        |
| Oracle     | 21c           |                        |
| SQL Server | 2017-CU12     |                        |
| PostgreSQL | 16-3.4-alpine |                        |
| DB2        | 11.5.0.0a     | progressiveStreaming=2 |
| SQLite     | 3.45.3        |                        |
| Informix   | 14.10         |                        |
| GBase      | 8s            | DELIMIDENT=y;          |
| 达梦         | DM8           |                        |
| TiDB       | v7.1.5        |                        |

**文档型数据库**

| 数据库名称   | 兼容版本 | 连接参数 |
|---------|------|------|
| MongoDB | 5.0  |      |

以上测试结果，可适用于同类型高版本数据库，后续将会支持更多数据库适配

## 基本概念

### 实体（Entity）

实体（Entity）是指现实中有形的事物，在关系型数据库中对应数据表（Table），在MongoDB中对应集合（Collection）

### 字段（Field）

字段即实体的属性，目前已经内置一些常用的字段类型，开发者也可根据业务情况进行扩展

##### 内置的字段类型

| 类型       | 名称   | Java类型映射                |
|----------|------|-------------------------|
| string   | 字符串  | java.lang.String        |
| text     | 文本   | java.lang.String        |
| int      | 整型   | java.lang.Integer       |
| bigint   | 长整型  | java.lang.Long          |
| decimal  | 小数   | java.lang.Double        |
| boolean  | 布尔   | java.lang.Boolean       |
| datetime | 日期时间 | java.time.LocalDateTime |
| date     | 日期   | java.time.LocalDate     |
| json     | JSON | N/A                     |
| id       | ID   | N/A                     |
| relation | 关系   | N/A                     |

### 视图（View）

视图是一个虚拟的表，其内容由查询定义，视图本身不支持存储数据，而是保存了查询语句。

### 索引（Index）

索引相当于一本书的目录，用于提高查询效率。

### 序列（Sequence）

序列是指按照一定规律增加的数字，一般作为唯一标识符使用，可以与年月日或特定字符组合使用。

### 验证器（Validator）

验证器是对用户提交的数据进行合法性验证，开发者可自定义验证器来支持业务功能。

### 值生成器（ValueGenerator）

值生成器可用于默认值计算，开发者可自定义值生成器来支持业务功能。

## 集成

要求Java版本为Java21，低版本不受支持

*引入maven依赖*

基本依赖

```xml
<dependencies>
  <groupId>tech.wetech.flexmodel</groupId>
  <artifactId>flexmodel-core</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependencies>
```

FlexModel本身不提供数据库的驱动连接的支持，所以还需要引入数据库厂商依赖，以MySQL为例

```xml
<!-- 数据库驱动 -->
<dependencies>
  <dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.3.0</version>
    <scope>test</scope>
  </dependency>
<!-- 任意数据库连接池 -->  
  <dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>5.1.0</version>
    <scope>test</scope>
  </dependency>
</dependencies>
```

配置

```java
public class Simple {
  public static void main(String[] args) {
    // 新建连接池
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/test_db");
    dataSource.setUsername("root");
    dataSource.setPassword("123456");
    // 通过数据源名称获取对应的数据源
    String dsName = "mysql";

    // 设置默认数据源
    JdbcDataSourceProvider jdbcDataSourceProvider = new JdbcDataSourceProvider(dataSource);
    SessionFactory sessionFactory = SessionFactory.builder()
      .setDefaultDataSourceProvider(jdbcDataSourceProvider)
      .build();
    // 创建会话，开始使用
    Session session = sessionFactory.createSession(dsName);
  }
}
```

## 使用

### 实体管理

创建实体

```java
session.createEntity("teacher", entity -> entity
  // 主键，当主键为整形字段时支持设置自增，也可以使用字符串配合值计算器生成
  .addField(new IDField("id").setComment("Primary Key"))
  // 姓名
  .addField(new StringField("name").setComment("姓名").setNullable(false).setLength(10))
  // 年龄，支持设置验证器进行业务验证
  .addField(new IntField("age").setComment("年龄").addValidator(new NumberRangeValidator<>(1, 300)))
  // 备注
  .addField(new TextField("description").setComment("备注"))
  // 生日
  .addField(new DateField("birthday").setComment("生日"))
  // 是否禁用
  .addField(new BooleanField("isLocked").setNullable(false).setDefaultValue(false).setComment("是否禁用"))
  // 创建时间，设置默认值等于当前时间
  .addField(new DatetimeField("createDatetime").setComment("创建日期时间").addCalculation(new DatetimeNowValueCalculator()))
  // 扩展信息
  .addField(new JsonField("extra").setComment("扩展信息"))
  // 创建索引
  .addIndex(index -> index.addField("name", Direction.DESC).addField("id"))
  .setComment("教师表")
);

```

#### 创建字段

字段即实体的属性

```java
// 创建实体时至少要有一个ID字段
session.createEntity("teacher_course", sScore -> sScore
  .addField(new IDField<>("id").setGeneratedValue(DefaultGeneratedValue.AUTO_INCREMENT).setComment("Primary Key"))
  .setComment("教师成绩表")
);
// 创建字段
session.createField(entityName, new StringField("c_name"));
session.createField(entityName, new DecimalField("c_score"));

```

新增关联关系字段
```java
// 此功能会建立外键约束
session.createField("teacher", new RelationField("teacher_course")
  .setCardinality(ONE_TO_MANY) // 关联方式，如果是ONE_TO_ONE时，外键字段的会存在唯一索引约束，当关联关系是MANY_TO_MANY时，会建立中间表保存多对多的关联关系
  .setCascadeDelete(true) // 级联删除，依赖于外键字段 on delete cascade
  .setTargetEntity("teacher_course") // 目标实体
  .setTargetField("teacher_id")  // 目标字段
);
```

删除字段

```java
session.dropField("teacher_course", "name");
```

删除实体

```java
session.dropModel(entityName);
```

创建索引

```java
// 单个字段索引
Index index = new Index("teacher", "IDX_name");
index.addField("name");
// 是否唯一
index.setUnique(true);
// 索引验证提示
index.setValidMessage("名称字段必须唯一");
session.createIndex(index);
// 组合索引
// when include multiple field
Index multipleFiledIndex = new Index("teacher");
multipleFiledIndex.addField("birthday");
multipleFiledIndex.addField("age", DESC);
multipleFiledIndex.addField("is_deleted", DESC);
multipleFiledIndex.setName("IDX_compound");
session.createIndex(multipleFiledIndex);
```

删除索引

```java
session.dropIndex(modelName, indexName);
```

### 视图管理

创建视图

```java
session.createView("teacher_course_report", "teacher", query -> query
      .setProjection(projection -> projection
          .addField("teacher_id", field("id"))
          .addField("teacher_name", field("name"))
          .addField("age_max", max(field("age")))
          .addField("age_count", count(field("age")))
      )
      .setJoins(joiners -> joiners
        .addLeftJoin(joiner -> joiner
          .setLocalField("id") // 主键字段，存在关联关系时可不指定
          .setForeignField("teacher_id") // 外键字段，存在关联关系时可不指定
          .setFrom(teacherCourseEntityName)
        )
      )
      .setFilter(f -> f.greaterThanOrEqualTo("id", 1))
      .setGroupBy(groupBy -> groupBy
        .addField("id")
        .addField("teacher_name")
      )
      .setSort(sort -> sort.addOrder("id", Direction.DESC))
      .setPage(1, 1000)
  );

```

查询视图数据

```java
List<Map<String, Object>> list = session.find("teacher_course_report", query -> query
  .setFilter(f -> f.equalTo("teacher_id", 2))
);
```

删除视图

```java
session.dropModel("teacher_course_report"); // 同删除实体
```

### 序列

#### 新增序列

```java
String sequenceName = "user_seq";
int initialValue = 1;
int incrementSize = 1;
session.createSequence(seqName, initialValue, incrementSize);
```

#### 获取序列下一个值

```java
long sequenceNextVal = session.getSequenceNextVal(sequenceName);
```

#### 删除序列

```java
session.dropSequence(sequenceName);
```

### 增删改查

#### 新增

新增一条数据

```java
Map<String, Object> record = new HashMap<>();
record.put("name", "张三丰");
record.put("age", 218);
record.put("description", "武当山道士");
record.put("createDatetime", LocalDateTime.now());
record.put("birthday", LocalDate.of(1247, 1, 1));
record.put("isLocked", true);
Assertions.assertEquals(1, session.insert(entityName, record));
```

新增多条数据

```java
String data = """
      [
        { "teacher_id": 1, "c_name": "语文", "c_score": 92 },
        { "teacher_id": 2, "c_name": "数学", "c_score": 78 },
        { "teacher_id": 3, "c_name": "英语", "c_score": 85 }
      ]
      """;
    List<Map<String, Object>> records = JsonUtils.getInstance().parseToObject(data, List.class);
    session.insertAll(entityName, records);
```

#### 查询

查询示例

```java
List<Map<String, Object>> groupList = session.find(entityName, query -> query
  // 设置查询字段，不设置则查询模型（实体/视图）的所有字段
  .setProjection(projection -> projection
    .addField("teacher_name", field("name"))
    // 支持的函数见 tech.wetech.flexmodel.Projections 类
    .addField("course_count", count(field(courseEntityName + ".teacher_id")))
    .addField("course_score_sum", sum(field(courseEntityName + ".c_score")))
  )
  // 设置关联表
  .setJoins(joiners -> joiners
    .addInnerJoin(joiner -> joiner
      .setFrom(courseEntityName)
      .setLocalField("id") // 主键字段，存在关联关系时可不指定
      .setForeignField("teacher_id") // 外键字段，存在关联关系时可不指定
      .setFilter(f -> f.notEqualTo("teacher_id", 999))
    )
  )
  // 设置分组
  .setGroupBy(groupBy -> groupBy
    .addField("teacher_name")
  )
  // 设置过滤条件
  .setFilter(f -> f.equalTo("username", "john_doe")
    .or()
    .equalTo("remark", "aa")
    .equalTo("locked", false)
    .notEqualTo("email", "jane_doe@example.com")
    .greaterThan("age", 18)
    .and()
    .greaterThanOrEqualTo("registrationDate", "2020-01-01")
    .lessThan("age", 65)
    .lessThanOrEqualTo("lastLogin", "2023-01-01")
    .or(or -> or.notIn("role", List.of("banned")).in("status", List.of("active", "pending")))
    .between("createdAt", "2022-01-01", "2022-12-31")
  )
  // 设置去重的字段(暂未实现)
  .setDistintOn("teacher_id", "teacher_name")
  // 设置排序
  .setSort(sort -> sort.addOrder("id", Direction.DESC))
  // 设置分页查询
  .setPage(1, 1000)
);

```

日期时间格式化

```java
// 支持yyyy-MM-dd hh:mm:ss格式的日期时间格式化
List<Map<String, Object>> dateFormatList = session.find(entityName, query -> query
  .setProjection(projection -> projection
    .addField("datetime", dateFormat(field("birthday"), "yyyy/MM/dd hh:mm:ss"))
    .addField("user_count", count(field("id"))))
  .setGroupBy(groupBy ->
    groupBy.addField("datetime") // 根据别名进行分组
  )
);
```

按照一年中的天数分组

```java
List<Map<String, Object>> dayOfYearList = session.find(entityName, query -> query
  .setProjection(projection -> projection
    .addField("dayOfYear", dayOfYear(field("birthday")))
    .addField("user_count", count(field("id"))))
  .setGroupBy(groupBy ->
    groupBy.addField("dayOfYear")
  )
);
```

按照一月中的天数进行分组

```java
Assertions.assertFalse(dayOfYearList.isEmpty());
List<Map<String, Object>> dayOfMonthList = session.find(entityName, query -> query
  .setProjection(projection -> projection
    .addField("dayOfMonth", dayOfMonth(field("birthday")))
    .addField("user_count", count(field("id"))))
  .setGroupBy(groupBy ->
    groupBy.addField("dayOfMonth")
  )
);

```

按照一周中的天数进行分组

```java
List<Map<String, Object>> dayOfWeekList = session.find(entityName, query -> query
  .setProjection(projection -> projection
    .addField("dayOfWeek", dayOfWeek(field("birthday")))
    .addField("user_count", count(field("id"))))
  .setGroupBy(groupBy ->
    groupBy.addField("dayOfWeek")
  )
);
```

查询返回实体类

```java
List<TeacherDTO> list = session.find(entityName, query -> query, TeacherDTO.class);
```

#### 更新

根据ID更新

```java
Map<String, Object> record = new HashMap<>();
record.put("name", "李白");
record.put("age", 61);
record.put("description", "字太白，号青莲居士");
Long id = 999;
int affectedRows = session.updateById(entityName, record2, id);
```

#### 删除

根据ID删除

```java
session.deleteById(entityName, 1);
```

#### 更多使用示例请见测试用例

#### FlexModel 目前还在测试阶段，变更比较频繁，请勿用于生产环境，否则后果自负
