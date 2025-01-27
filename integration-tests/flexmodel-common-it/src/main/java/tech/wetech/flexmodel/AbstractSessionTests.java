package tech.wetech.flexmodel;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.dto.TeacherDTO;
import tech.wetech.flexmodel.sql.JdbcDataSourceProvider;
import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tech.wetech.flexmodel.Direction.DESC;
import static tech.wetech.flexmodel.IDField.GeneratedValue.*;
import static tech.wetech.flexmodel.Projections.*;

/**
 * @author cjbi
 */
public abstract class AbstractSessionTests {

  public static SessionFactory sessionFactory;
  public static Session session;
  private final JsonObjectConverter jsonObjectConverter = new JacksonObjectConverter();

  protected static void initSession(DataSourceProvider dataSourceProvider) {
    sessionFactory = SessionFactory.builder()
      .setDefaultDataSourceProvider("default", dataSourceProvider)
      .build();
    session = sessionFactory.createSession("default");
  }

  @AfterAll
  static void afterAll() {

  }

  void createClassesEntity(String entityName) {
    session.createEntity(entityName, entity -> entity
      .addField(new IDField("id").setGeneratedValue(BIGINT_NOT_GENERATED))
      .addField(new StringField("classCode"))
      .addField(new StringField("className"))
    );
  }

  void createStudentCollection(String entityName) {
    Enum genderEnum = session.createEnum(entityName + "_gender", en ->
      en.addElement("男")
        .addElement("女")
        .setComment("性别")
    );
    Enum interestEnum = session.createEnum(entityName + "_interest", en ->
      en.addElement("唱")
        .addElement("跳")
        .addElement("rap")
        .addElement("打篮球")
        .setComment("兴趣")
    );
    session.createEntity(entityName, entity -> entity
      .addField(new IDField("id").setGeneratedValue(BIGINT_NOT_GENERATED))
      .addField(new StringField("studentName"))
      .addField(new EnumField("gender").setFrom(genderEnum.getName()))
      .addField(new EnumField("interest").setFrom(interestEnum.getName()).setMultiple(true))
      .addField(new IntField("age"))
      .addField(new DecimalField("height").setPrecision(3).setScale(2))
      .addField(new BigintField("classId"))
    );
  }

  void createStudentDetailCollection(String entityName) {
    session.createEntity(entityName, entity -> entity
      .addField(new IDField("id").setGeneratedValue(AUTO_INCREMENT))
      .addField(new BigintField("studentId"))
      .addField(new TextField("description"))
    );
  }

  void createCourseCollection(String entityName) {
    session.createEntity(entityName, entity -> entity
      .addField(new IDField("courseNo").setGeneratedValue(STRING_NOT_GENERATED))
      .addField(new StringField("courseName"))
    );
  }

  void createTeacherCollection(String entityName) {
    session.createEntity(entityName, entity -> entity
      .addField(new IDField("id").setGeneratedValue(BIGINT_NOT_GENERATED))
      .addField(new StringField("teacherName"))
      .addField(new StringField("subject"))
    );
  }

  void createAssociations(String classRoomEntityName, String studentEntityName,
                          String studentDetailEntityName, String courseEntityName, String teacherEntityName) {
    // 班级:学生
    session.createField(
      new RelationField("students")
        .setModelName(classRoomEntityName)
        .setFrom(studentEntityName)
        .setForeignField("classId")
        .setMultiple(true)
        .setCascadeDelete(true)
    );
    // 学生:学生明细 -> 1:1
    session.createField(
      new RelationField("studentDetail")
        .setModelName(studentEntityName)
        .setFrom(studentDetailEntityName)
        .setForeignField("studentId")
        .setMultiple(false)
    );
  }

  void createClassesData(String entityName) {
    String mockData = """
        [
          {
            "id": 1,
            "className": "一年级1班",
            "classCode": "C_001"
          },
          {
            "id": 2,
            "className": "一年级2班",
            "classCode": "C_002"
          },
          {
            "id": 3,
            "className": "二年级1班",
            "classCode": "C_003"
          }
        ]
      """;
    List<Map<String, Object>> list = jsonObjectConverter.parseToMapList(mockData);
    session.insertAll(entityName, list);
  }

  void createStudentData(String entityName) {
    String mockData = """
      [
        {
          "id": 1,
          "studentName": "张三",
          "gender": "男",
          "interest": ["唱", "跳", "rap", "打篮球"],
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
          "gender": "女",
          "interest": ["唱", "跳"],
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
          "interest": ["打篮球"],
          "gender": "男",
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
    session.insertAll(entityName, list);
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
    session.insertAll(entityName, list);
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
    session.insertAll(entityName, list);
  }

  @Test
  void testRelation() {
    String classesEntityName = "TestRelationClasses";
    String studentEntityName = "TestRelationStudent";
    String studentDetailEntityName = "TestRelationStudentDetail";
    String courseEntityName = "TestRelationCourse";
    String teacherEntityName = "TestRelationTeacher";
    createClassesEntity(classesEntityName);
    createStudentCollection(studentEntityName);
    createStudentDetailCollection(studentDetailEntityName);
    createCourseCollection(courseEntityName);
    createTeacherCollection(teacherEntityName);
    createAssociations(classesEntityName, studentEntityName, studentDetailEntityName, courseEntityName, teacherEntityName);
    createCourseData(courseEntityName);
    createClassesData(classesEntityName);
    createStudentData(studentEntityName);
    createTeacherData(teacherEntityName);

    // 1:1
    Map<String, Object> oneToOne = session.find(studentEntityName, query -> query
      .withProjection(projection -> projection
        .addField("studentName", field("studentName"))
        .addField("description", field(studentDetailEntityName + ".description"))
      )
      .withJoin(joins -> joins
        .addLeftJoin(join -> join.setFrom(studentDetailEntityName))
      )
      .withFilter(f -> f.equalTo(studentEntityName + ".id", 1))
    ).getFirst();
    Assertions.assertEquals("张三", oneToOne.get("studentName"));
    Assertions.assertEquals("张三的描述", oneToOne.get("description"));

    // 1:n
    List<Map<String, Object>> oneToMany = session.find(classesEntityName, query -> query
      .withProjection(projection -> projection
        .addField("className", field("className"))
        .addField("studentName", field(studentEntityName + ".studentName"))
      )
      .withJoin(joins -> joins
        .addLeftJoin(join -> join
          .setFrom(studentEntityName)
        )
      )
      .withFilter(f -> f.equalTo(classesEntityName + ".id", 1))
    );
    Assertions.assertFalse(oneToMany.isEmpty());
    Assertions.assertEquals("一年级1班", oneToMany.getFirst().get("className"));
    Assertions.assertEquals("张三", oneToMany.getFirst().get("studentName"));

  }

  @Test
  void testEnum() {
    String studentEntityName = "testEnumStudent";
    createStudentCollection(studentEntityName);
    createStudentData(studentEntityName);
    List<Map> list = session.find("testEnumStudent", q -> q, Map.class);
    Assertions.assertFalse(list.isEmpty());
    Map first = list.getFirst();
    Assertions.assertInstanceOf(String.class, first.get("gender"));
    Assertions.assertInstanceOf(Collection.class, first.get("interest"));
  }

  void createTeacherCollection2(String entityName) {
    session.createEntity(entityName, entity -> entity
      // 主键
      .addField(new IDField("id").setGeneratedValue(IDField.GeneratedValue.AUTO_INCREMENT).setComment("Primary Key"))
      // 姓名
      .addField(new StringField("name").setComment("姓名").setNullable(false).setLength(10))
      // 年龄
      .addField(new IntField("age").setComment("年龄"))
      // 备注
      .addField(new TextField("description").setComment("备注"))
      // 生日
      .addField(new DateField("birthday").setComment("生日"))
      // 是否禁用
      .addField(new BooleanField("isLocked").setNullable(false).setDefaultValue(false).setComment("是否禁用"))
      // 创建时间
      .addField(new DatetimeField("createDatetime").setComment("创建日期时间"))
      // 扩展信息
      .addField(new JsonField("extra").setComment("扩展信息"))
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
    session.insertAll(entityName, list);
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
    session.createEntity(teacherCourseEntity, sScore -> sScore
      .addField(new IDField("id"))
      .addField(new StringField("c_name"))
      .addField(new DecimalField("c_score"))
      .addField(new BigintField("teacher_id"))
      .setComment("教师成绩表")
    );
    session.createField(
      new RelationField("courses")
        .setModelName(teacherEntityName)
        .setMultiple(true)
        .setCascadeDelete(true)
        .setFrom(teacherCourseEntity)
        .setForeignField("teacher_id")
    );
    session.insertAll(teacherCourseEntity, list);
  }

  @Test
  void testInsert() {
    String entityName = "testInsert_teacher";
    createTeacherCollection2(entityName);
    Map<String, Object> record = new HashMap<>();
    record.put("name", "张三丰");
    record.put("age", 218);
    record.put("description", "武当山道士");
    record.put("createDatetime", LocalDateTime.now());
    record.put("birthday", LocalDate.of(1247, 1, 1));
    record.put("isLocked", true);
//    record.put("extra", """
//      [{
//         "foo": "bar"
//      }]
//      """);
    Assertions.assertEquals(1, session.insert(entityName, record));
    Map<String, Object> record2 = new HashMap<>();
    record2.put("name", "李白");
    record2.put("age", 61);
    record2.put("description", "字太白，号青莲居士");
    record2.put("createDatetime", LocalDateTime.now());
    record2.put("birthday", LocalDate.of(701, 2, 28));
    record2.put("extra", Map.of("foo", "bar"));
    Assertions.assertEquals(1, session.insert(entityName, record2, Assertions::assertNotNull));
    Map<String, Object> record3 = new HashMap<>();
    record3.put("name", "杜甫");
    record3.put("age", 58);
    record3.put("description", "字子美，自号少陵野老，唐代伟大的现实主义诗人");
    record3.put("birthday", LocalDate.of(712, 2, 12));
    record3.put("extra", null);
    Assertions.assertEquals(1, session.insert(entityName, record3, Assertions::assertNotNull));
  }

  @Test
  void testInsertAll() {
    String entityName = "testInsertAll_teacher";
    createTeacherCollection2(entityName);
  }

  @Test
  void testUpdate() {
    String entityName = "testUpdate_teacher";
    createTeacherCollection2(entityName);
    Map<String, Object> record = new HashMap<>();
    record.put("name", "李白");
    record.put("age", 61);
    record.put("description", "字太白，号青莲居士；是一名刺客型英雄");
    int affectedRows = session.updateById(entityName, record, 2);
    Assertions.assertEquals(1, affectedRows);
    Map<String, Object> record2 = new HashMap<>();
    // record 中有没有id都不影响结果
    record2.put("id", 999);
    record2.put("name", "李白");
    record2.put("age", 61);
    record2.put("description", "字太白，号青莲居士；是一名刺客型英雄");
    int affectedRows2 = session.updateById(entityName, record2, 999);
    Assertions.assertEquals(0, affectedRows2);
  }

  @Test
  void testFindById() {
    String entityName = "testFindById_teacher";
    createTeacherCollection2(entityName);
    Map<String, Object> record = session.findById(entityName, 1);
    Assertions.assertFalse(record.isEmpty());
    Map<String, Object> record2 = session.findById(entityName, 999);
    Assertions.assertNull(record2);
  }

  @Test
  void testFind() {
    String entityName = "testFind_teacher";
    createTeacherCollection2(entityName);
    List<Map<String, Object>> list = session.find(entityName, query ->
      query
        .withProjection(project -> project
          .addField("teacher_id", field("id"))
          .addField("teacher_name", field("name"))
        )
        .withFilter(f -> f.or(or -> or.equalTo("name", "张三").equalTo("name", "李四"))));
    Assertions.assertFalse(list.isEmpty());
    Assertions.assertEquals(2, list.size());
  }

  @Test
  void testFindByQueryConditionWhenHasGroupBy() {
    String entityName = "testFindByQueryConditionWhenHasGroupBy_teacher";
    String courseEntityName = "testFindByQueryConditionWhenHasGroupBy_teacher_courses";
    createTeacherCollection2(entityName);
    createTeacherCourseEntity(entityName, courseEntityName);
    // 聚合分组
    List<Map<String, Object>> groupList = session.find(entityName, query -> query
      .withProjection(projection -> projection
        .addField("teacher_name", field("name"))
        .addField("course_count", count(field(courseEntityName + ".teacher_id")))
        .addField("course_score_sum", sum(field(courseEntityName + ".c_score")))
      )
      .withJoin(joiners -> joiners
        .addInnerJoin(joiner -> joiner
          .setFrom(courseEntityName)
          .setFilter("""
            {
              "teacher_id": {
                "_ne": 999
              }
            }
            """)
        )
      )
      .withGroupBy(groupBy -> groupBy
        .addField("teacher_name")
      )
      .withFilter(f -> f.equalTo("name", "李四"))
    );
    Assertions.assertFalse(groupList.isEmpty());
    Map<String, Object> groupFirst = groupList.getFirst();
    Assertions.assertEquals("李四", groupFirst.get("teacher_name"));
    Assertions.assertEquals(4, ((Number) groupFirst.get("course_count")).intValue());
    Assertions.assertEquals(338, ((Number) groupFirst.get("course_score_sum")).intValue());
    // group by time
    List<Map<String, Object>> dateFormatList = session.find(entityName, query -> query
      .withProjection(projection -> projection
        .addField("year", dateFormat(field("birthday"), "yyyy-MM-dd hh:mm:ss"))
        .addField("user_count", count(field("id"))))
      .withGroupBy(groupBy ->
        groupBy.addField("year")
      )
    );
    Assertions.assertFalse(dateFormatList.isEmpty());
    List<Map<String, Object>> dateFormatList1 = session.find(entityName, query -> query
      .withProjection(projection -> projection
        .addField("year", dateFormat(field("birthday"), "yyyy-MM-dd"))
        .addField("user_count", count(field("id"))))
      .withGroupBy(groupBy ->
        groupBy.addField("year")
      )
    );
    Assertions.assertFalse(dateFormatList1.isEmpty());
    List<Map<String, Object>> dateFormatList2 = session.find(entityName, query -> query
      .withProjection(projection -> projection
        .addField("year", dateFormat(field("createDatetime"), "yyyy-MM-dd hh:mm:ss"))
        .addField("user_count", count(field("id"))))
      .withGroupBy(groupBy ->
        groupBy.addField("year")
      )
    );
    Assertions.assertFalse(dateFormatList2.isEmpty());
    List<Map<String, Object>> dayOfYearList = session.find(entityName, query -> query
      .withProjection(projection -> projection
        .addField("dayOfYear", dayOfYear(field("birthday")))
        .addField("user_count", count(field("id"))))
      .withGroupBy(groupBy ->
        groupBy.addField("dayOfYear")
      )
    );
    Assertions.assertFalse(dayOfYearList.isEmpty());
    List<Map<String, Object>> dayOfMonthList = session.find(entityName, query -> query
      .withProjection(projection -> projection
        .addField("dayOfMonth", dayOfMonth(field("birthday")))
        .addField("user_count", count(field("id"))))
      .withGroupBy(groupBy ->
        groupBy.addField("dayOfMonth")
      )
    );
    Assertions.assertFalse(dayOfMonthList.isEmpty());
    List<Map<String, Object>> dayOfWeekList = session.find(entityName, query -> query
      .withProjection(projection -> projection
        .addField("dayOfWeek", dayOfWeek(field("birthday")))
        .addField("user_count", count(field("id"))))
      .withGroupBy(groupBy ->
        groupBy.addField("dayOfWeek")
      )
    );
    Assertions.assertFalse(dayOfWeekList.isEmpty());
  }

  @Test
  void testCountByCondition() {
    String entityName = "testCountByCondition_teacher";
    createTeacherCollection2(entityName);
    long total = session.count(entityName, query -> query
      .withFilter(f -> f.or(or -> or.equalTo("name", "张三").equalTo("name", "李四"))));
    Assertions.assertEquals(2, total);
  }

  @Test
  void testExistsByCondition() {
    String entityName = "testExistsByCondition_teacher";
    createTeacherCollection2(entityName);
    boolean exists = session.exists(entityName, query -> query.setFilter("""
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
    createTeacherCollection2(entityName);
    List<Map<String, Object>> list = session.find(entityName, query -> query);
    Assertions.assertFalse(list.isEmpty());
  }

  @Test
  void testFindAllWhenResultDto() {
    String entityName = "testFindAllWhenResultDto_teacher";
    createTeacherCollection2(entityName);
    List<TeacherDTO> list = session.find(entityName, query -> query, TeacherDTO.class);
    Assertions.assertFalse(list.isEmpty());
    Assertions.assertNotNull(list.getFirst().getId());
  }

  @Test
  void testCountAll() {
    String entityName = "testCountAll_teacher";
    createTeacherCollection2(entityName);
    Assertions.assertTrue(session.exists(entityName, query -> query));
  }

  @Test
  void testExistsById() {
    String entityName = "testExistsById_teacher";
    createTeacherCollection2(entityName);
    Assertions.assertTrue(session.existsById(entityName, 1));
  }

  @Test
  void testDeleteById() {
    String entityName = "testDeleteById_teacher";
    createTeacherCollection2(entityName);
    int affectedRows = session.deleteById(entityName, 1);
    Assertions.assertEquals(1, affectedRows);
    int affectedRows2 = session.deleteById(entityName, 2);
    Assertions.assertEquals(1, affectedRows2);
    int affectedRows3 = session.deleteById(entityName, 999);
    Assertions.assertEquals(0, affectedRows3);
  }

  @Test
  void testDelete() {
    String entityName = "testDelete_teacher";
    createTeacherCollection2(entityName);
    int affectedRows = session.delete(entityName, """
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
    createTeacherCollection2(entityName);
    int affectedRows = session.deleteAll(entityName);
    Assertions.assertTrue(affectedRows > 0);
  }

  @Test
  void testQueryNoProjection() {
    String entityName = "testRelation_teacher";
    String courseEntityName = "testRelation_teacher_courses";
    createTeacherCollection2(entityName);
    createTeacherCourseEntity(entityName, courseEntityName);
    List<Map<String, Object>> list = session.find(entityName, query -> query
      .withProjection(projection -> projection
        .addField("teacher_name", field("name")))
      .withSort(sort -> sort.addOrder("id")
      ));
    Assertions.assertFalse(list.isEmpty());
    Map<String, Object> first = list.getFirst();
    Assertions.assertEquals("张三", first.get("teacher_name"));
  }


  void createStudentCollection2(String entityName) {
    Entity entity = session.createEntity(
      entityName, e -> e.setComment("学生")
        .addField(new IDField("id").setGeneratedValue(IDField.GeneratedValue.AUTO_INCREMENT).setComment("Primary Key"))
    );
    // string
    StringField name = new StringField("name");
    name.setModelName(entityName);
    name.setComment("姓名");
    name.setNullable(false);
    name.setLength(10);
    entity.addField(name);
    session.createField(name);
    // text
    TextField description = new TextField("description").setComment("备注");
    description.setModelName(entityName);
    entity.addField(description);
    session.createField(description);
    // number
    IntField age = new IntField("age");
    age.setModelName(entityName);
    age.setComment("年龄");
    entity.addField(age);
    session.createField(age);
    // boolean
    BooleanField deleted = new BooleanField("is_deleted");
    deleted.setModelName(entityName);
    deleted.setComment("软删除");
    deleted.setDefaultValue(false);
    entity.addField(deleted);
    session.createField(deleted);
    // datetime
    DatetimeField createDatetime = new DatetimeField("createDatetime");
    createDatetime.setModelName(entityName);
    createDatetime.setComment("创建日期时间");
    entity.addField(createDatetime);
    session.createField(createDatetime);
    // date
    DateField birthday = new DateField("birthday");
    birthday.setModelName(entityName);
    birthday.setComment("出生日期");
    entity.addField(birthday);
    session.createField(birthday);
    // json
    JsonField interests = new JsonField("interests");
    interests.setModelName(entityName);
    interests.setComment("兴趣爱好");
    entity.addField(interests);
    session.createField(interests);
  }

  private void createScoreEntity2(String scoreModelName) {
    session.createEntity(scoreModelName, sScore ->
      sScore.addField(new BigintField("student_id"))
        .addField(new StringField("course_name"))
        .addField(new DecimalField("score"))
    );
  }

  void dropModel(String entityName) {
    session.dropModel(entityName);
  }

  @Test
  void testCreateCollection() {
    createStudentCollection2("testCreateEntity_holiday");
  }

  @Test
  void testDropEntity() {
    String entityName = "testDropEntity_holiday";
    createStudentCollection2(entityName);
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
    createStudentCollection(studentEntityName);
    createStudentDetailCollection(studentDetailEntityName);
    createCourseCollection(courseEntityName);
    createTeacherCollection(teacherEntityName);
    createAssociations(classesEntityName, studentEntityName, studentDetailEntityName, courseEntityName, teacherEntityName);
    createCourseData(courseEntityName);
    createClassesData(classesEntityName);
    createStudentData(studentEntityName);
    createTeacherData(teacherEntityName);
    Assertions.assertEquals(4, ((Entity) session.getModel(classesEntityName)).getFields().size());
    session.modifyField(new StringField("classCode").setModelName(classesEntityName).setLength(100));
    Assertions.assertEquals(4, ((Entity) session.getModel(classesEntityName)).getFields().size());
    session.modifyField(new TextField("className").setModelName(classesEntityName));
    Assertions.assertEquals(4, ((Entity) session.getModel(classesEntityName)).getFields().size());
  }

  @Test
  void testCreateField() {
    String entityName = "testCreateField_students";
    createStudentCollection2(entityName);
    dropModel(entityName);
  }

  @Test
  void testDropField() {
    String entityName = "testDropField_students";
    createStudentCollection2(entityName);
    session.createIndex(new Index(entityName, "IDX_name").addField("name"));
    session.createIndex(
      new Index(entityName, "IDX_age_is_deleted")
        .addField("name")
        .addField("is_deleted")
        .setUnique(true)
    );
    session.dropIndex(entityName, "IDX_name");
    session.dropIndex(entityName, "IDX_age_is_deleted");
    session.dropField(entityName, "name");
    session.dropField(entityName, "description");
    session.dropField(entityName, "age");
    session.dropField(entityName, "is_deleted");
    session.dropField(entityName, "createDatetime");
    session.dropField(entityName, "birthday");
    Entity entity = (Entity) session.getModel(entityName);
    Assertions.assertNull(entity.getField("name"));
    Assertions.assertNull(entity.getField("description"));
    Assertions.assertNull(entity.getField("age"));
    Assertions.assertNull(entity.getField("is_deleted"));
    Assertions.assertNull(entity.getField("createDatetime"));
    Assertions.assertNull(entity.getField("birthday"));
    Assertions.assertNull(entity.getIndex("IDX_name"));
    Assertions.assertNull(entity.getIndex("IDX_age_is_deleted"));
    dropModel(entityName);
    Assertions.assertNull(session.getModel(entityName));
  }

  @Test
  void testCreateIndex() {
    String entityName = "testDropField_students";
    createStudentCollection2(entityName);
    // when include single field
    Index index = new Index(entityName, "IDX_name");
    index.setModelName(entityName);
    index.addField("name");
    session.createIndex(index);
    session.dropIndex(entityName, "IDX_name");
    // when include multiple field
    Index multipleFiledIndex = new Index(entityName);
    multipleFiledIndex.setModelName(entityName);
    multipleFiledIndex.addField("birthday");
    multipleFiledIndex.addField("age", DESC);
    multipleFiledIndex.addField("is_deleted", DESC);
    multipleFiledIndex.setName("IDX_compound");
    session.createIndex(multipleFiledIndex);
    session.dropIndex(entityName, "IDX_compound");
    Entity entity = (Entity) session.getModel(entityName);
    Assertions.assertNull(entity.getIndex("IDX_compound"));
    dropModel(entityName);
  }

  @Test
  void testCreateSequence() {
    String seqName = "user_seq";
    session.createSequence(seqName, 1, 1);
    long sequenceNextVal = 0;
    for (int i = 0; i < 10; i++) {
      sequenceNextVal = session.getSequenceNextVal(seqName);
    }
    Assertions.assertEquals(10, sequenceNextVal);
    session.dropSequence(seqName);
  }

  @Test
  void testSyncModels() {
    String classesEntityName = "TestSyncModelsClasses";
    String studentEntityName = "TestSyncModelsStudent";
    String studentDetailEntityName = "TestSyncModelsStudentDetail";
    String courseEntityName = "TestSyncModelsCourse";
    String teacherEntityName = "TestSyncModelsTeacher";
    createClassesEntity(classesEntityName);
    createStudentCollection(studentEntityName);
    createStudentDetailCollection(studentDetailEntityName);
    createCourseCollection(courseEntityName);
    createTeacherCollection(teacherEntityName);
    createAssociations(classesEntityName, studentEntityName, studentDetailEntityName, courseEntityName, teacherEntityName);
    createCourseData(courseEntityName);
    createClassesData(classesEntityName);
    createStudentData(studentEntityName);
    createTeacherData(teacherEntityName);
    createTeacherCollection2("TestSyncModelsteacher2");
    DataSourceProvider dataSourceProvider = sessionFactory.getDataSourceProvider("system");
    String identifier = "sync_test";
    sessionFactory.addDataSourceProvider(identifier, dataSourceProvider);
    if (dataSourceProvider instanceof JdbcDataSourceProvider jdbcDataSourceProvider) {
      SessionFactory sessionFactory = SessionFactory.builder()
        .setDefaultDataSourceProvider(jdbcDataSourceProvider)
        .build();
      sessionFactory.addDataSourceProvider(identifier, dataSourceProvider);
      Session newSession = sessionFactory.createSession(identifier);
      List<TypeWrapper> models = newSession.syncModels();
      Assertions.assertFalse(models.isEmpty());
      Entity studentDetailEntity = (Entity) models.stream().filter(m -> m.getName().equals(studentDetailEntityName)).findFirst().get();
      Assertions.assertFalse(studentDetailEntity.getFields().isEmpty());
      Assertions.assertFalse(studentDetailEntity.getIndexes().isEmpty());
    } else {
      // todo mongodb模型同步待实现
    }

  }

  @Test
  void testLoadScript() {
    sessionFactory.loadScript("default", "import.json");
  }

}
