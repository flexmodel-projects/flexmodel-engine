package tech.wetech.flexmodel;

import org.junit.jupiter.api.*;
import tech.wetech.flexmodel.entity.Classes;
import tech.wetech.flexmodel.entity.Student;
import tech.wetech.flexmodel.entity.StudentDetail;
import tech.wetech.flexmodel.model.EntityDefinition;
import tech.wetech.flexmodel.model.EnumDefinition;
import tech.wetech.flexmodel.model.IndexDefinition;
import tech.wetech.flexmodel.model.SchemaObject;
import tech.wetech.flexmodel.model.field.*;
import tech.wetech.flexmodel.query.Direction;
import tech.wetech.flexmodel.query.Expressions;
import tech.wetech.flexmodel.query.Predicate;
import tech.wetech.flexmodel.query.Query;
import tech.wetech.flexmodel.session.Session;
import tech.wetech.flexmodel.session.SessionFactory;
import tech.wetech.flexmodel.sql.JdbcDataSourceProvider;
import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static tech.wetech.flexmodel.query.Direction.DESC;
import static tech.wetech.flexmodel.query.Query.*;


/**
 * @author cjbi
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractSessionTests {

    public static SessionFactory sessionFactory;
    public static Session session;
    private final JsonObjectConverter jsonObjectConverter = new JacksonObjectConverter();

    // 用于生成唯一的实体名称，避免测试间冲突
    private final AtomicInteger entityCounter = new AtomicInteger(1);

    // 记录测试中创建的实体，用于清理
    private final List<String> createdEntities = new ArrayList<>();

    protected static void initSession(DataSourceProvider dataSourceProvider) {
        sessionFactory = SessionFactory.builder()
            .setDefaultDataSourceProvider(dataSourceProvider)
            .build();
        session = sessionFactory.createSession("default");
    }

    @BeforeEach
    void setUp() {
        // 每个测试方法执行前的准备工作
        createdEntities.clear();
    }

    @AfterEach
    void tearDown() {
        // 清理测试中创建的实体
        for (String entityName : createdEntities) {
            try {
                session.schema().dropModel(entityName);
            } catch (Exception e) {
                // 忽略清理时的异常，避免影响测试结果
            }
        }
        createdEntities.clear();
    }

    @AfterAll
    static void afterAll() {
        // 清理静态资源
        if (session != null) {
            try {
                // 如果Session有close方法则调用，否则忽略
                if (session.getClass().getMethod("close") != null) {
                    session.close();
                }
            } catch (Exception e) {
                // 忽略关闭时的异常
            }
        }
    }

    /**
     * 生成唯一的实体名称，避免测试间冲突
     */
    protected String generateEntityName(String baseName) {
        return baseName + "_" + entityCounter.getAndIncrement();
    }

    /**
     * 记录创建的实体，用于测试后清理
     */
    protected void registerEntity(String entityName) {
        createdEntities.add(entityName);
    }

    /**
     * 创建简单的查询对象，减少lambda使用
     */
    private Query createSimpleQuery() {
        return new Query();
    }

    /**
     * 创建带条件的查询对象
     */
    private Query createQueryWithCondition(Predicate condition) {
        return Query.Builder.create()
            .where(condition)
            .build();
    }

    /**
     * 创建带投影的查询对象
     */
    private Query createQueryWithSelect(String... fields) {
        return Query.Builder.create()
            .select(fields)
            .build();
    }

    /**
     * 演示不同的查询构建方式，减少lambda使用
     */
    private void demonstrateQueryBuilding() {
        String entityName = "demo_entity";

        // 方式1：使用Query.Builder（推荐，减少lambda）
        Query query1 = Query.Builder.create()
            .select("id", "name", "age")
            .where(Expressions.field("age").gte(18))
            .orderBy(orderBy -> orderBy.asc("name"))
            .page(1, 10)
            .build();

        // 方式2：使用辅助方法
        Query query2 = createQueryWithCondition(Expressions.field("age").gte(18));

        // 方式3：使用简单查询
        Query query3 = createSimpleQuery();

        // 方式4：使用QueryBuilder的静态方法（如果可用）
        // Query query4 = QueryBuilder.simpleQuery("id", "name");
    }

    void createClassesEntity(String entityName) {
        session.schema().createEntity(entityName, entity -> entity
            .addField(new StringField("id").asIdentity().setDefaultValue(DefaultValue.ULID))
            .addField(new StringField("classCode"))
            .addField(new StringField("className"))
        );
    }

    void createStudentEntity(String entityName) {
        EnumDefinition genderEnum = session.schema().createEnum(entityName + "_gender", en ->
            en.addElement("UNKNOWN")
                .addElement("MALE")
                .addElement("FEMALE")
                .setComment("性别")
        );
        EnumDefinition interestEnum = session.schema().createEnum(entityName + "_interest", en ->
            en.addElement("chang")
                .addElement("tiao")
                .addElement("rap")
                .addElement("daLanQiu")
                .setComment("兴趣")
        );
        session.schema().createEntity(entityName, entity -> entity
            .addField(new StringField("id").asIdentity())
            .addField(new StringField("studentName"))
            .addField(new EnumField("gender").setFrom(genderEnum.getName()))
            .addField(new EnumField("interest").setFrom(interestEnum.getName()).setMultiple(true))
            .addField(new IntField("age"))
            .addField(new FloatField("height").setPrecision(3).setScale(2))
            .addField(new StringField("classId"))
        );
    }

    void createStudentDetailEntity(String entityName) {
        session.schema().createEntity(entityName, entity -> entity
            .addField(new LongField("id").asIdentity().setDefaultValue(DefaultValue.AUTO_INCREMENT))
            .addField(new StringField("studentId"))
            .addField(new StringField("description"))
        );
    }

    void createCourseEntity(String entityName) {
        session.schema().createEntity(entityName, entity -> entity
            .addField(new StringField("courseNo").asIdentity().setDefaultValue(DefaultValue.UUID))
            .addField(new StringField("courseName"))
        );
    }

    void createTeacherEntity(String entityName) {
        session.schema().createEntity(entityName, entity -> entity
            .addField(new StringField("id").asIdentity().setDefaultValue(DefaultValue.AUTO_INCREMENT))
            .addField(new StringField("teacherName"))
            .addField(new StringField("subject"))
        );
    }

    void createAssociations(String classRoomEntityName, String studentEntityName,
                            String studentDetailEntityName, String courseEntityName, String teacherEntityName) {
        // 班级:学生
        session.schema().createField(
            new RelationField("students")
                .setModelName(classRoomEntityName)
                .setFrom(studentEntityName)
                .setLocalField("id")
                .setForeignField("classId")
                .setMultiple(true)
                .setCascadeDelete(true)
        );
        // 学生:学生明细 -> 1:1
        session.schema().createField(
            new RelationField("studentDetail")
                .setModelName(studentEntityName)
                .setFrom(studentDetailEntityName)
                .setLocalField("id")
                .setForeignField("studentId")
                .setMultiple(false)
        );
        // 明细:学生 -> 1:1
        session.schema().createField(
            new RelationField("student")
                .setModelName(studentDetailEntityName)
                .setFrom(studentEntityName)
                .setLocalField("studentId")
                .setForeignField("id")
                .setMultiple(false)
        );
    }

    void createClassesData(String entityName) {
        String mockData = """
              [
                {
                  "id": "1",
                  "className": "一年级1班",
                  "classCode": "C_001"
                },
                {
                  "id": "2",
                  "className": "一年级2班",
                  "classCode": "C_002"
                },
                {
                  "id": "3",
                  "className": "二年级1班",
                  "classCode": "C_003"
                }
              ]
            """;
        List<Map<String, Object>> list = jsonObjectConverter.parseToMapList(mockData);
        session.data().insertAll(entityName, list);
    }

    void createStudentData(String entityName) {
        String mockData = """
            [
              {
                "id": 1,
                "studentName": "张三",
                "gender": "MALE",
                "interest": ["chang", "tiao", "rap", "daLanQiu"],
                "age": 10,
                "classId": 1,
                "studentDetail": {
                  "description": "张三的描述"
                },
                "courses": [
                   {
                     "courseNo":"Math",
                     "courseName":"数学"
                   },
                   {
                     "courseNo":"YuWen",
                     "courseName":"语文"
                   },
                   {
                     "courseNo":"Eng",
                     "courseName":"英语"
                   }
                ]
              },
              {
                "id": 2,
                "studentName": "李四",
                "gender": "FEMALE",
                "interest": ["chang", "tiao"],
                "age": 10,
                "classId": 1,
                "studentDetail": {
                  "description": "李四的描述"
                },
                "courses": [
                   {
                     "courseNo":"Math",
                     "courseName":"数学"
                   },
                   {
                     "courseNo":"YuWen",
                     "courseName":"语文"
                   }
                ]
              },
              {
                "id": 3,
                "studentName": "王五",
                "interest": ["daLanQiu"],
                "gender": "MALE",
                "age": 11,
                "classId": 2,
                "studentDetail": {
                  "description": "王五的描述"
                },
                "courses": [
                   {
                     "courseNo":"YuWen",
                     "courseName":"语文"
                   },
                   {
                     "courseNo":"Eng",
                     "courseName":"英语"
                   }
                ]
              }
            ]
            """;
        List<Map<String, Object>> list = jsonObjectConverter.parseToMapList(mockData);
        session.data().insertAll(entityName, list);
    }

    void createCourseData(String entityName) {
        String mockData = """
              [
                 {
                   "courseNo": "Math",
                   "courseName": "数学"
                 },
                 {
                   "courseNo": "YuWen",
                   "courseName": "语文"
                 },
                 {
                   "courseNo": "Eng",
                   "courseName": "英语"
                 },
                 {
                   "courseNo": "History",
                   "courseName": "历史"
                 },
                 {
                   "courseNo": "politics",
                   "courseName": "政治"
                 }
              ]
            """;
        List<Map<String, Object>> list = jsonObjectConverter.parseToMapList(mockData);
        session.data().insertAll(entityName, list);
    }

    void createTeacherData(String entityName) {
        String mockData = """
            [
              {
                "id": 1,
                "teacherName": "张老师",
                "subject": "数学",
                "students": [
                   {
                     "id": 1
                   },
                   {
                     "id": 2
                   },
                   {
                     "id": 3
                   }
                ]
              },
              {
                "id": 2,
                "teacherName": "李老师",
                "subject": "语文",
                "students": [
                   {
                     "id": 1
                   },
                   {
                     "id": 2
                   },
                   {
                     "id": 3
                   }
                ]
              },
              {
                "id": 3,
                "teacherName": "王老师",
                "subject": "英语",
                "students": [
                   {
                     "id": 1
                   },
                   {
                     "id": 3
                   }
                ]
              }
            ]
            """;
        List<Map<String, Object>> list = jsonObjectConverter.parseToMapList(mockData);
        session.data().insertAll(entityName, list);
    }

    @Test
    void testRelation() {
        // 使用唯一实体名称避免测试冲突
        String classesEntityName = generateEntityName("TestRelationClasses");
        String studentEntityName = generateEntityName("TestRelationStudent");
        String studentDetailEntityName = generateEntityName("TestRelationStudentDetail");
        String courseEntityName = generateEntityName("TestRelationCourse");
        String teacherEntityName = generateEntityName("TestRelationTeacher");

        // 创建实体并注册用于清理
        createClassesEntity(classesEntityName);
        registerEntity(classesEntityName);
        createStudentEntity(studentEntityName);
        registerEntity(studentEntityName);
        createStudentDetailEntity(studentDetailEntityName);
        registerEntity(studentDetailEntityName);
        createCourseEntity(courseEntityName);
        registerEntity(courseEntityName);
        createTeacherEntity(teacherEntityName);
        registerEntity(teacherEntityName);

        // 创建关联关系
        createAssociations(classesEntityName, studentEntityName, studentDetailEntityName, courseEntityName, teacherEntityName);

        // 插入测试数据
        createCourseData(courseEntityName);
        createClassesData(classesEntityName);
        createStudentData(studentEntityName);
        createTeacherData(teacherEntityName);

        // 测试1:1关系查询
        testOneToOneRelation(studentEntityName, studentDetailEntityName);

        // 测试1:n关系查询
        testOneToManyRelation(classesEntityName, studentEntityName);
    }

    /**
     * 测试1:1关系查询
     */
    private void testOneToOneRelation(String studentEntityName, String studentDetailEntityName) {
        Query query = Query.Builder.create()
            .select(select -> select
                .field("studentName", "studentName")
                .field("description", studentDetailEntityName + ".description")
            )
            .leftJoin(join -> join
                .model(studentDetailEntityName)
            )
            .where(Expressions.field(studentEntityName + ".id").eq("1"))
            .build();

        Map<String, Object> oneToOne = session.data().find(studentEntityName, query).getFirst();

        // 详细的断言验证
        Assertions.assertNotNull(oneToOne, "查询结果不应为空");
        Assertions.assertEquals("张三", oneToOne.get("studentName"), "学生姓名应该匹配");
        Assertions.assertEquals("张三的描述", oneToOne.get("description"), "学生描述应该匹配");
    }

    /**
     * 测试1:n关系查询
     */
    private void testOneToManyRelation(String classesEntityName, String studentEntityName) {
        Query query = Query.Builder.create()
            .select(select -> select
                .field("className", "className")
                .field("studentName", studentEntityName + ".studentName")
            )
            .leftJoin(join -> join
                .model(studentEntityName)
            )
            .where(Expressions.field(classesEntityName + ".id").eq("1"))
            .build();

        List<Map<String, Object>> oneToMany = session.data().find(classesEntityName, query);

        // 详细的断言验证
        Assertions.assertFalse(oneToMany.isEmpty(), "查询结果不应为空");
        Assertions.assertTrue(oneToMany.size() >= 1, "应该至少有一条记录");

        Map<String, Object> firstRecord = oneToMany.getFirst();
        Assertions.assertEquals("一年级1班", firstRecord.get("className"), "班级名称应该匹配");
        Assertions.assertEquals("张三", firstRecord.get("studentName"), "学生姓名应该匹配");
    }

    @Test
    void testEnum() {
        String studentEntityName = "testEnumStudent";
        createStudentEntity(studentEntityName);
        createStudentData(studentEntityName);
        List<Map<String, Object>> list = session.dsl().select().from("testEnumStudent").execute();
        Assertions.assertFalse(list.isEmpty());
        Map first = list.getFirst();
        Assertions.assertInstanceOf(String.class, first.get("gender"));
        Assertions.assertInstanceOf(Collection.class, first.get("interest"));
    }

    void createTeacher2(String entityName) {
        session.schema().createEntity(entityName, entity -> entity
            // 主键
            .addField(new LongField("id").asIdentity().setDefaultValue(DefaultValue.AUTO_INCREMENT).setComment("Primary Key"))
            // 姓名
            .addField(new StringField("name").setComment("姓名").setNullable(false).setLength(10))
            // 年龄
            .addField(new IntField("age").setComment("年龄"))
            // 备注
            .addField(new StringField("description").setComment("备注"))
            // 生日
            .addField(new DateField("birthday").setComment("生日"))
            // 是否禁用
            .addField(new BooleanField("isLocked").setNullable(false).setFixedDefaultValue(false).setComment("是否禁用"))
            // 创建时间
            .addField(new DateTimeField("createDatetime").setComment("创建日期时间"))
            // 扩展信息
            .addField(new JSONField("extra").setComment("扩展信息"))
            // 创建索引
            .addIndex(index -> index.addField("name", Direction.DESC).addField("id"))
            .setComment("教师表")
        );

        String mockData = """
            [
              {
                "birthday": "1995-03-15",
                "isLocked": false,
                "name": "张三",
                "description": "软件工程师",
                "age": 25
              },
              {
                "birthday": "1995-03-28",
                "isLocked": true,
                "name": "李四",
                "description": "市场营销经理",
                "age": 37
              },
              {
                "birthday": "1991-01-12",
                "isLocked": false,
                "name": "王五",
                "description": "人力资源专员",
                "age": 42
              },
              {
                "birthday": "1965-01-07",
                "isLocked": true,
                "name": "赵六",
                "description": "退休教师",
                "age": 55
              },
              {
                "birthday": "1991-10-23",
                "isLocked": false,
                "name": "孙七",
                "description": "设计师",
                "age": 29
              },
              {
                "birthday": "1995-05-01",
                "isLocked": true,
                "name": "周八",
                "description": "产品经理",
                "age": 32
              },
              {
                "birthday": "1991-10-20",
                "isLocked": false,
                "name": "吴九",
                "description": "会计",
                "age": 45
              }
            ]
            """;
        List<Map<String, Object>> list = jsonObjectConverter.parseToMapList(mockData);
        session.data().insertAll(entityName, list);
    }

    void createTeacherCourseEntity(String teacherEntityName, String teacherCourseEntity) {
        String mockData = """
            [
              { "teacher_id": 1, "c_name": "语文", "c_score": 92 },
              { "teacher_id": 2, "c_name": "数学", "c_score": 78 },
              { "teacher_id": 3, "c_name": "英语", "c_score": 85 },
              { "teacher_id": 4, "c_name": "物理", "c_score": 95 },
              { "teacher_id": 5, "c_name": "化学", "c_score": 81 },
              { "teacher_id": 1, "c_name": "历史", "c_score": 72 },
              { "teacher_id": 2, "c_name": "地理", "c_score": 88 },
              { "teacher_id": 3, "c_name": "生物", "c_score": 90 },
              { "teacher_id": 4, "c_name": "政治", "c_score": 86 },
              { "teacher_id": 5, "c_name": "体育", "c_score": 75 },
              { "teacher_id": 1, "c_name": "美术", "c_score": 83 },
              { "teacher_id": 2, "c_name": "音乐", "c_score": 79 },
              { "teacher_id": 3, "c_name": "信息技术", "c_score": 87 },
              { "teacher_id": 4, "c_name": "心理学", "c_score": 91 },
              { "teacher_id": 5, "c_name": "哲学", "c_score": 76 },
              { "teacher_id": 1, "c_name": "经济学", "c_score": 82 },
              { "teacher_id": 2, "c_name": "社会学", "c_score": 93 },
              { "teacher_id": 3, "c_name": "法语", "c_score": 80 },
              { "teacher_id": 4, "c_name": "德语", "c_score": 74 },
              { "teacher_id": 5, "c_name": "西班牙语", "c_score": 89 }
            ]
            """;
        List<Map<String, Object>> list = jsonObjectConverter.parseToMapList(mockData);
        session.schema().createEntity(teacherCourseEntity, sScore -> sScore
            .addField(new LongField("id").asIdentity().setDefaultValue(DefaultValue.AUTO_INCREMENT))
            .addField(new StringField("c_name"))
            .addField(new FloatField("c_score"))
            .addField(new LongField("teacher_id"))
            .setComment("教师成绩表")
        );
        session.schema().createField(
            new RelationField("courses")
                .setModelName(teacherEntityName)
                .setMultiple(true)
                .setCascadeDelete(true)
                .setFrom(teacherCourseEntity)
                .setForeignField("teacher_id")
        );
        session.data().insertAll(teacherCourseEntity, list);
    }

    @Test
    void testInsert() {
        String entityName = generateEntityName("testInsert_teacher");
        createTeacher2(entityName);
        registerEntity(entityName);

        // 测试插入基本数据
        testInsertBasicRecord(entityName);

        // 测试插入带扩展信息的数据
        testInsertRecordWithExtra(entityName);

        // 测试插入null值
        testInsertRecordWithNull(entityName);
    }

    /**
     * 测试插入基本记录
     */
    private void testInsertBasicRecord(String entityName) {
        Map<String, Object> record = new HashMap<>();
        record.put("name", "张三丰");
        record.put("age", 218);
        record.put("description", "武当山道士");
        record.put("createDatetime", LocalDateTime.now());
        record.put("birthday", LocalDate.of(1247, 1, 1));
        record.put("isLocked", true);

        int affectedRows = session.data().insert(entityName, record);
        Assertions.assertEquals(1, affectedRows, "应该成功插入一条记录");

        // 验证插入的数据
        Map<String, Object> insertedRecord = session.data().findById(entityName, record.get("id"));
        Assertions.assertNotNull(insertedRecord, "插入的记录应该能够被查询到");
        Assertions.assertEquals("张三丰", insertedRecord.get("name"), "姓名应该匹配");
        Assertions.assertEquals(218, insertedRecord.get("age"), "年龄应该匹配");
    }

    /**
     * 测试插入带扩展信息的记录
     */
    private void testInsertRecordWithExtra(String entityName) {
        Map<String, Object> record = new HashMap<>();
        record.put("name", "李白");
        record.put("age", 61);
        record.put("description", "字太白，号青莲居士");
        record.put("createDatetime", LocalDateTime.now());
        record.put("birthday", LocalDate.of(701, 2, 28));
        record.put("extra", Map.of("foo", "bar"));

        int affectedRows = session.data().insert(entityName, record);
        Assertions.assertEquals(1, affectedRows, "应该成功插入一条记录");

        // 验证扩展信息
        Map<String, Object> insertedRecord = session.data().findById(entityName, record.get("id"));
        Assertions.assertNotNull(insertedRecord.get("extra"), "扩展信息不应为空");
    }

    /**
     * 测试插入null值
     */
    private void testInsertRecordWithNull(String entityName) {
        Map<String, Object> record = new HashMap<>();
        record.put("name", "杜甫");
        record.put("age", 58);
        record.put("description", "字子美，自号少陵野老，唐代伟大的现实主义诗人");
        record.put("birthday", LocalDate.of(712, 2, 12));
        record.put("extra", null);

        int affectedRows = session.data().insert(entityName, record);
        Assertions.assertEquals(1, affectedRows, "应该成功插入一条记录");
        Assertions.assertNotNull(record.get("id"), "ID不能为null");

        // 验证null值处理
        Map<String, Object> insertedRecord = session.data().findById(entityName, record.get("id"));
        Assertions.assertNull(insertedRecord.get("extra"), "扩展信息应该为null");
    }

    @Test
    void testInsertAll() {
        String entityName = "testInsertAll_teacher";
        createTeacher2(entityName);
    }

    @Test
    void testUpdate() {
        String entityName = "testUpdate_teacher";
        createTeacher2(entityName);
        Map<String, Object> record = new HashMap<>();
        record.put("name", "李白");
        record.put("age", 61);
        record.put("description", "字太白，号青莲居士；是一名刺客型英雄");
        int affectedRows = session.data().updateById(entityName, record, 2);
        Assertions.assertEquals(1, affectedRows);
        Map<String, Object> record2 = new HashMap<>();
        // record 中有没有id都不影响结果
        record2.put("id", 999);
        record2.put("name", "李白");
        record2.put("age", 61);
        record2.put("description", "字太白，号青莲居士；是一名刺客型英雄");
        int affectedRows2 = session.data().updateById(entityName, record2, 999);
        Assertions.assertEquals(0, affectedRows2);
    }

    @Test
    void testFindById() {
        String entityName = "testFindById_teacher";
        createTeacher2(entityName);
        Map<String, Object> record = session.data().findById(entityName, 1);
        Assertions.assertFalse(record.isEmpty());
        Map<String, Object> record2 = session.data().findById(entityName, 999);
        Assertions.assertNull(record2);
    }

    @Test
    void testFind() {
        String entityName = "testFind_teacher";
        createTeacher2(entityName);
        Query query = Query.Builder.create()
            .select(select -> select
                .field("teacher_id", "id")
                .field("teacher_name", "name")
            )
            .where(Expressions.field("name").eq("张三").or(Expressions.field("name").eq("李四")))
            .build();

        List<Map<String, Object>> list = session.data().find(entityName, query);
        Assertions.assertFalse(list.isEmpty());
        Assertions.assertEquals(2, list.size());
    }

    @Test
    void testFindByQueryConditionWhenHasGroupBy() {
        String entityName = "testFindByQueryConditionWhenHasGroupBy_teacher";
        String courseEntityName = "testFindByQueryConditionWhenHasGroupBy_teacher_courses";
        createTeacher2(entityName);
        createTeacherCourseEntity(entityName, courseEntityName);
        // 聚合分组
        List<Map<String, Object>> groupList = session.data().find(entityName, query -> query
            .select(projection -> projection
                .field("teacher_name", field("name"))
                .field("course_count", count(field(courseEntityName + ".teacher_id")))
                .field("course_score_sum", sum(field(courseEntityName + ".c_score")))
            )
            .innerJoin(joiners -> joiners
                .model(courseEntityName)
                .where("""
                      {
                        "teacher_id": {
                          "_ne": 999
                        }
                      }
                    """))
            .groupBy(groupBy -> groupBy
                .field("teacher_name")
            )
            .where(Expressions.field("name").eq("李四"))
        );
        Assertions.assertFalse(groupList.isEmpty());
        Map<String, Object> groupFirst = groupList.getFirst();
        Assertions.assertEquals("李四", groupFirst.get("teacher_name"));
        Assertions.assertEquals(4, ((Number) groupFirst.get("course_count")).intValue());
        Assertions.assertEquals(338, ((Number) groupFirst.get("course_score_sum")).intValue());
        // group by time
        List<Map<String, Object>> dateFormatList = session.data().find(entityName, query -> query
            .select(projection -> projection
                .field("year", dateFormat(field("birthday"), "yyyy-MM-dd hh:mm:ss"))
                .field("user_count", count(field("id"))))
            .groupBy(groupBy ->
                groupBy.field("year")
            )
        );
        Assertions.assertFalse(dateFormatList.isEmpty());
        List<Map<String, Object>> dateFormatList1 = session.data().find(entityName, query -> query
            .select(projection -> projection
                .field("year", dateFormat(field("birthday"), "yyyy-MM-dd"))
                .field("user_count", count(field("id"))))
            .groupBy(groupBy ->
                groupBy.field("year")
            )
        );
        Assertions.assertFalse(dateFormatList1.isEmpty());
        List<Map<String, Object>> dateFormatList2 = session.data().find(entityName, query -> query
            .select(projection -> projection
                .field("year", dateFormat(field("createDatetime"), "yyyy-MM-dd hh:mm:ss"))
                .field("user_count", count(field("id"))))
            .groupBy(groupBy ->
                groupBy.field("year")
            )
        );
        Assertions.assertFalse(dateFormatList2.isEmpty());
        List<Map<String, Object>> dayOfYearList = session.data().find(entityName, query -> query
            .select(projection -> projection
                .field("dayOfYear", dayOfYear(field("birthday")))
                .field("user_count", count(field("id"))))
            .groupBy(groupBy ->
                groupBy.field("dayOfYear")
            )
        );
        Assertions.assertFalse(dayOfYearList.isEmpty());
        List<Map<String, Object>> dayOfMonthList = session.data().find(entityName, query -> query
            .select(projection -> projection
                .field("dayOfMonth", dayOfMonth(field("birthday")))
                .field("user_count", count(field("id"))))
            .groupBy(groupBy ->
                groupBy.field("dayOfMonth")
            )
        );
        Assertions.assertFalse(dayOfMonthList.isEmpty());
        List<Map<String, Object>> dayOfWeekList = session.data().find(entityName, query -> query
            .select(projection -> projection
                .field("dayOfWeek", dayOfWeek(field("birthday")))
                .field("user_count", count(field("id"))))
            .groupBy(groupBy ->
                groupBy.field("dayOfWeek")
            )
        );
        Assertions.assertFalse(dayOfWeekList.isEmpty());
    }

    @Test
    void testCountByCondition() {
        String entityName = "testCountByCondition_teacher";
        createTeacher2(entityName);
        Query query = Query.Builder.create()
            .where(Expressions.field("name").eq("张三").or(Expressions.field("name").eq("李四")))
            .build();

        long total = session.data().count(entityName, query);
        Assertions.assertEquals(2, total);
    }

    @Test
    void testExistsByCondition() {
        String entityName = "testExistsByCondition_teacher";
        createTeacher2(entityName);
        boolean exists = session.data().exists(entityName, query -> query.where("""
            {
              "_or": [
                {
                  "name": {
                    "_eq": "张三"
                  }
                },
                {
                  "name": {
                    "_eq": "李四"
                  }
                }
              ]
            }
            """));
        Assertions.assertTrue(exists);
    }

    @Test
    void testFindAll() {
        String entityName = "testFindAll_teacher";
        createTeacher2(entityName);
        List<Map<String, Object>> list = session.data().find(entityName, createSimpleQuery());
        Assertions.assertFalse(list.isEmpty());
    }

    @Test
    void testFindAllWhenResultDto() {
        String entityName = "testFindAllWhenResultDto_teacher";
        createTeacher2(entityName);
        List<TeacherDTO> list = session.data().find(entityName, createSimpleQuery(), TeacherDTO.class);
        Assertions.assertFalse(list.isEmpty());
        Assertions.assertNotNull(list.getFirst().getId());
    }

    @Test
    void testTestInsertWhenDtoParam() {
        String entityName = "testTestInsertWhenDtoParam_teacher";
        createTeacher2(entityName);
        TeacherDTO teacherDTO = new TeacherDTO();
        teacherDTO.setName("张三");
        teacherDTO.setDescription("张三描述");
        teacherDTO.setAge(18);
        teacherDTO.setBirthday(LocalDate.now());
        teacherDTO.setExtra(Map.of("extra", "extra"));
        teacherDTO.setLocked(true);
        teacherDTO.setCreateDatetime(LocalDateTime.now());
        session.dsl()
            .insertInto(entityName)
            .values(teacherDTO)
            .execute();
        Assertions.assertNotNull(teacherDTO.getId(), "ID不能为null");
    }

    @Test
    void testCountAll() {
        String entityName = "testCountAll_teacher";
        createTeacher2(entityName);
        Assertions.assertTrue(session.data().exists(entityName, query -> query));
    }

    @Test
    void testExistsById() {
        String entityName = "testExistsById_teacher";
        createTeacher2(entityName);
        Assertions.assertTrue(session.data().existsById(entityName, 1));
    }

    @Test
    void testDeleteById() {
        String entityName = "testDeleteById_teacher";
        createTeacher2(entityName);
        int affectedRows = session.data().deleteById(entityName, 1);
        Assertions.assertEquals(1, affectedRows);
        int affectedRows2 = session.data().deleteById(entityName, 2);
        Assertions.assertEquals(1, affectedRows2);
        int affectedRows3 = session.data().deleteById(entityName, 999);
        Assertions.assertEquals(0, affectedRows3);
    }

    @Test
    void testDelete() {
        String entityName = "testDelete_teacher";
        createTeacher2(entityName);
        int affectedRows = session.data().delete(entityName, """
            {
              "id": {
                "_eq": 999
              }
            }
            """);
        Assertions.assertEquals(0, affectedRows);
    }

    @Test
    void testDeleteAll() {
        String entityName = "testDeleteAll_teacher";
        createTeacher2(entityName);
        int affectedRows = session.data().deleteAll(entityName);
        Assertions.assertTrue(affectedRows > 0);
    }

    @Test
    void testQueryNoProjection() {
        String entityName = "testRelation_teacher";
        String courseEntityName = "testRelation_teacher_courses";
        createTeacher2(entityName);
        createTeacherCourseEntity(entityName, courseEntityName);
        Query query = Query.Builder.create()
            .select(select -> select
                .field("teacher_name", "name")
            )
            .orderBy(orderBy -> orderBy.asc("id"))
            .build();

        List<Map<String, Object>> list = session.data().find(entityName, query);
        Assertions.assertFalse(list.isEmpty());
        Map<String, Object> first = list.getFirst();
        Assertions.assertEquals("张三", first.get("teacher_name"));
    }


    void createStudent2(String entityName) {
        EntityDefinition entity = session.schema().createEntity(
            entityName, e -> e.setComment("学生")
                .addField(new LongField("id").asIdentity().setDefaultValue(DefaultValue.AUTO_INCREMENT).setComment("Primary Key"))
        );
        // string
        StringField name = new StringField("name");
        name.setModelName(entityName);
        name.setComment("姓名");
        name.setNullable(false);
        name.setLength(10);
        session.schema().createField(name);
        // text
        StringField description = new StringField("description").setComment("备注");
        description.setModelName(entityName);
        session.schema().createField(description);
        // number
        IntField age = new IntField("age");
        age.setModelName(entityName);
        age.setComment("年龄");
        session.schema().createField(age);
        // boolean
        BooleanField deleted = new BooleanField("is_deleted");
        deleted.setModelName(entityName);
        deleted.setComment("软删除");
        deleted.setFixedDefaultValue(false);
        session.schema().createField(deleted);
        // datetime
        DateTimeField createDatetime = new DateTimeField("createDatetime");
        createDatetime.setModelName(entityName);
        createDatetime.setComment("创建日期时间");
        session.schema().createField(createDatetime);
        // date
        DateField birthday = new DateField("birthday");
        birthday.setModelName(entityName);
        birthday.setComment("出生日期");
        session.schema().createField(birthday);
        // json
        JSONField interests = new JSONField("interests");
        interests.setModelName(entityName);
        interests.setComment("兴趣爱好");
        session.schema().createField(interests);
    }

    private void createScoreEntity2(String scoreModelName) {
        session.schema().createEntity(scoreModelName, sScore ->
            sScore.addField(new LongField("student_id"))
                .addField(new StringField("course_name"))
                .addField(new FloatField("score"))
        );
    }

    void dropModel(String entityName) {
        session.schema().dropModel(entityName);
    }

    @Test
    void testCreateEntity() {
        createStudent2("testCreateEntity_holiday");
    }

    @Test
    void testDropEntity() {
        String entityName = "testDropEntity_holiday";
        createStudent2(entityName);
        dropModel(entityName);
    }

    @Test
    void testModifyField() {
        String classesEntityName = "TestModifyFieldClasses";
        String studentEntityName = "TestModifyFieldStudent";
        String studentDetailEntityName = "TestModifyFieldStudentDetail";
        String courseEntityName = "TestModifyFieldCourse";
        String teacherEntityName = "TestModifyFieldTeacher";
        createClassesEntity(classesEntityName);
        createStudentEntity(studentEntityName);
        createStudentDetailEntity(studentDetailEntityName);
        createCourseEntity(courseEntityName);
        createTeacherEntity(teacherEntityName);
        createAssociations(classesEntityName, studentEntityName, studentDetailEntityName, courseEntityName, teacherEntityName);
        createCourseData(courseEntityName);
        createClassesData(classesEntityName);
        createStudentData(studentEntityName);
        createTeacherData(teacherEntityName);
        Assertions.assertEquals(4, ((EntityDefinition) session.schema().getModel(classesEntityName)).getFields().size());
        session.schema().modifyField(new StringField("classCode").setModelName(classesEntityName).setLength(100));
        Assertions.assertEquals(4, ((EntityDefinition) session.schema().getModel(classesEntityName)).getFields().size());
        session.schema().modifyField(new StringField("className").setModelName(classesEntityName));
        Assertions.assertEquals(4, ((EntityDefinition) session.schema().getModel(classesEntityName)).getFields().size());
    }

    @Test
    void testCreateField() {
        String entityName = "testCreateField_students";
        createStudent2(entityName);
        dropModel(entityName);
    }

    @Test
    void testDropField() {
        String entityName = "testDropField_students";
        createStudent2(entityName);
        session.schema().createIndex(new IndexDefinition(entityName, "IDX_name").addField("name"));
        session.schema().createIndex(
            new IndexDefinition(entityName, "IDX_age_is_deleted")
                .addField("name")
                .addField("is_deleted")
                .setUnique(true)
        );
        session.schema().dropIndex(entityName, "IDX_name");
        session.schema().dropIndex(entityName, "IDX_age_is_deleted");
        session.schema().dropField(entityName, "name");
        session.schema().dropField(entityName, "description");
        session.schema().dropField(entityName, "age");
        session.schema().dropField(entityName, "is_deleted");
        session.schema().dropField(entityName, "createDatetime");
        session.schema().dropField(entityName, "birthday");
        EntityDefinition entity = (EntityDefinition) session.schema().getModel(entityName);
        Assertions.assertNull(entity.getField("name"));
        Assertions.assertNull(entity.getField("description"));
        Assertions.assertNull(entity.getField("age"));
        Assertions.assertNull(entity.getField("is_deleted"));
        Assertions.assertNull(entity.getField("createDatetime"));
        Assertions.assertNull(entity.getField("birthday"));
        Assertions.assertNull(entity.getIndex("IDX_name"));
        Assertions.assertNull(entity.getIndex("IDX_age_is_deleted"));
        dropModel(entityName);
        Assertions.assertNull(session.schema().getModel(entityName));
    }

    @Test
    void testCreateIndex() {
        String entityName = "testDropField_students";
        createStudent2(entityName);
        // when include single field
        IndexDefinition index = new IndexDefinition(entityName, "IDX_name");
        index.setModelName(entityName);
        index.addField("name");
        session.schema().createIndex(index);
        session.schema().dropIndex(entityName, "IDX_name");
        // when include multiple field
        IndexDefinition multipleFiledIndex = new IndexDefinition(entityName);
        multipleFiledIndex.setModelName(entityName);
        multipleFiledIndex.addField("birthday");
        multipleFiledIndex.addField("age", DESC);
        multipleFiledIndex.addField("is_deleted", DESC);
        multipleFiledIndex.setName("IDX_compound");
        session.schema().createIndex(multipleFiledIndex);
        session.schema().dropIndex(entityName, "IDX_compound");
        EntityDefinition entity = (EntityDefinition) session.schema().getModel(entityName);
        Assertions.assertNull(entity.getIndex("IDX_compound"));
        dropModel(entityName);
    }

    @Test
    void testCreateSequence() {
        String seqName = "user_seq";
        session.schema().createSequence(seqName, 1, 1);
        long sequenceNextVal = 0;
        for (int i = 0; i < 10; i++) {
            sequenceNextVal = session.schema().getSequenceNextVal(seqName);
        }
        Assertions.assertEquals(10, sequenceNextVal);
        session.schema().dropSequence(seqName);
    }

    @Test
    void testSyncModels() {
        String classesEntityName = "TestSyncModelsClasses";
        String studentEntityName = "TestSyncModelsStudent";
        String studentDetailEntityName = "TestSyncModelsStudentDetail";
        String courseEntityName = "TestSyncModelsCourse";
        String teacherEntityName = "TestSyncModelsTeacher";
        createClassesEntity(classesEntityName);
        createStudentEntity(studentEntityName);
        createStudentDetailEntity(studentDetailEntityName);
        createCourseEntity(courseEntityName);
        createTeacherEntity(teacherEntityName);
        createAssociations(classesEntityName, studentEntityName, studentDetailEntityName, courseEntityName, teacherEntityName);
        createCourseData(courseEntityName);
        createClassesData(classesEntityName);
        createStudentData(studentEntityName);
        createTeacherData(teacherEntityName);
        createTeacher2("TestSyncModelsteacher2");
        String identifier = "default";
        DataSourceProvider dataSourceProvider = sessionFactory.getDataSourceProvider(identifier);
//    sessionFactory.addDataSourceProvider(dataSourceProvider);
        if (dataSourceProvider instanceof JdbcDataSourceProvider jdbcDataSourceProvider) {
            SessionFactory sessionFactory = SessionFactory.builder()
                .setDefaultDataSourceProvider(jdbcDataSourceProvider)
                .build();
            sessionFactory.addDataSourceProvider(dataSourceProvider);
            Session newSession = sessionFactory.createSession(identifier);
            List<SchemaObject> models = newSession.schema().loadModels();
            Assertions.assertFalse(models.isEmpty());
            EntityDefinition studentDetailEntity = (EntityDefinition) models.stream().filter(m -> m.getName().equals(studentDetailEntityName)).findFirst().get();
            Assertions.assertFalse(studentDetailEntity.getFields().isEmpty());
            Assertions.assertTrue(studentDetailEntity.getIndexes().isEmpty());
        } else {
            // todo mongodb模型同步待实现
        }

    }

    @Test
    void testLoadScript() {
        sessionFactory.loadScript("default", "import.json");
    }

    @Test
    void testLazyLoad() {
        String classesEntityName = "testLazyLoad_Classes";
        String studentEntityName = "testLazyLoad_Student";
        String studentDetailEntityName = "testLazyLoad_StudentDetail";
        String courseEntityName = "testLazyLoad_Course";
        String teacherEntityName = "testLazyLoad_Teacher";
        createClassesEntity(classesEntityName);
        createStudentEntity(studentEntityName);
        createStudentDetailEntity(studentDetailEntityName);
        createCourseEntity(courseEntityName);
        createTeacherEntity(teacherEntityName);
        createAssociations(classesEntityName, studentEntityName, studentDetailEntityName, courseEntityName, teacherEntityName);
        createCourseData(courseEntityName);
        createClassesData(classesEntityName);
        createStudentData(studentEntityName);
        createTeacherData(teacherEntityName);
        List<Classes> classesList = session.data().find(classesEntityName, new Query(), Classes.class);
        for (Classes classes : classesList) {
            for (Student student : classes.getStudents()) {
                StudentDetail studentDetail = student.getStudentDetail();
                StudentDetail studentDetail2 = student.getStudentDetail();
                StudentDetail studentDetail3 = student.getStudentDetail();
                Assertions.assertNotNull(studentDetail);
                Assertions.assertNotNull(studentDetail2);
                Assertions.assertNotNull(studentDetail3);
                Assertions.assertNotNull(studentDetail.getStudent());
            }
        }
        // fixme 需要增加死循环检测
//    System.out.println(new JacksonObjectConverter().toJsonString(classesList));
    }

}
