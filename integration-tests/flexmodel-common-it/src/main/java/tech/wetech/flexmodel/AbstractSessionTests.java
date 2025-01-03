package tech.wetech.flexmodel;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.dto.TeacherDTO;
import tech.wetech.flexmodel.sql.JdbcDataSourceProvider;
import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tech.wetech.flexmodel.Direction.DESC;
import static tech.wetech.flexmodel.IDField.GeneratedValue.*;
import static tech.wetech.flexmodel.Projections.*;
import static tech.wetech.flexmodel.RelationField.Cardinality.*;

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

  void createStudentEntity(String entityName) {
    session.createEntity(entityName, entity -> entity
      .addField(new IDField("id").setGeneratedValue(BIGINT_NOT_GENERATED))
      .addField(new StringField("studentName"))
      .addField(new StringField("gender"))
      .addField(new IntField("age"))
      .addField(new DecimalField("height").setPrecision(3).setScale(2))
      .addField(new BigintField("classId"))
    );
  }

  void createStudentDetailEntity(String entityName) {
    session.createEntity(entityName, entity -> entity
      .addField(new IDField("id").setGeneratedValue(AUTO_INCREMENT))
      .addField(new BigintField("studentId"))
      .addField(new TextField("description"))
    );
  }

  void createCourseEntity(String entityName) {
    session.createEntity(entityName, entity -> entity
      .addField(new IDField("courseNo").setGeneratedValue(STRING_NOT_GENERATED))
      .addField(new StringField("courseName"))
    );
  }

  void createTeacherEntity(String entityName) {
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
        .setTargetEntity(studentEntityName)
        .setTargetField("classId")
        .setCardinality(ONE_TO_MANY)
        .setCascadeDelete(true)
    );
    // 学生:课程 -> n:n
    session.createField(
      new RelationField("courses")
        .setModelName(studentEntityName)
        .setTargetEntity(courseEntityName)
        .setTargetField("courseNo")
        .setCardinality(MANY_TO_MANY)
    );
    // 学生:学生明细 -> 1:1
    session.createField(
      new RelationField("studentDetail")
        .setModelName(studentEntityName)
        .setTargetEntity(studentDetailEntityName)
        .setTargetField("studentId")
        .setCardinality(ONE_TO_ONE)
    );
    // 学生:教师 -> n:n
    session.createField(
      new RelationField("teachers")
        .setModelName(studentEntityName)
        .setTargetEntity(teacherEntityName)
        .setTargetField("id")
        .setCardinality(MANY_TO_MANY)
    );
    // 教师:学生 -> n:n
    session.createField(
      new RelationField("students")
        .setModelName(teacherEntityName)
        .setTargetEntity(studentEntityName)
        .setTargetField("id")
        .setCardinality(MANY_TO_MANY)
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
    createStudentEntity(studentEntityName);
    createStudentDetailEntity(studentDetailEntityName);
    createCourseEntity(courseEntityName);
    createTeacherEntity(teacherEntityName);
    createAssociations(classesEntityName, studentEntityName, studentDetailEntityName, courseEntityName, teacherEntityName);
    createCourseData(courseEntityName);
    createClassesData(classesEntityName);
    createStudentData(studentEntityName);
    createTeacherData(teacherEntityName);

    // 1:1
    Map<String, Object> oneToOne = session.find(studentEntityName, query -> query
      .setProjection(projection -> projection
        .addField("studentName", field("studentName"))
        .addField("description", field(studentDetailEntityName + ".description"))
      )
      .setJoins(joins -> joins
        .addLeftJoin(join -> join.setFrom(studentDetailEntityName))
      )
      .setFilter(f -> f.equalTo(studentEntityName + ".id", 1))
    ).getFirst();
    Assertions.assertEquals("张三", oneToOne.get("studentName"));
    Assertions.assertEquals("张三的描述", oneToOne.get("description"));

    // 1:n
    List<Map<String, Object>> oneToMany = session.find(classesEntityName, query -> query
      .setProjection(projection -> projection
        .addField("className", field("className"))
        .addField("studentName", field(studentEntityName + ".studentName"))
      )
      .setJoins(joins -> joins
        .addLeftJoin(join -> join
          .setFrom(studentEntityName)
        )
      )
      .setFilter(f -> f.equalTo(classesEntityName + ".id", 1))
    );
    Assertions.assertFalse(oneToMany.isEmpty());
    Assertions.assertEquals("一年级1班", oneToMany.getFirst().get("className"));
    Assertions.assertEquals("张三", oneToMany.getFirst().get("studentName"));

    // n:n
    List<Map<String, Object>> manyToMany = session.find(studentEntityName, query -> query
      .setProjection(projection -> projection
        .addField("studentName", field("studentName"))
        .addField(teacherEntityName + ".teacherName", field("teacherName"))
      )
      .setJoins(joins -> joins.addLeftJoin(join -> join
        .setFrom(teacherEntityName)
      ))
      .setFilter(f -> f.equalTo(studentEntityName + ".id", 1))
    );
    Assertions.assertFalse(manyToMany.isEmpty());
    Assertions.assertEquals(3, manyToMany.size());

    // deep query list
    List<Map<String, Object>> deepList = session.find(classesEntityName, query -> query.setDeep(true));
    Assertions.assertFalse(deepList.isEmpty());
    Assertions.assertNotNull(deepList.getFirst().get("students"));
    // deep query by id
    Map<String, Object> deepData = session.findById(classesEntityName, 1, true);
    Assertions.assertNotNull(deepData.get("students"));
  }


  void createTeacherEntity2(String entityName) {
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
        .setCardinality(ONE_TO_MANY)
        .setCascadeDelete(true)
        .setTargetEntity(teacherCourseEntity)
        .setTargetField("teacher_id")
    );
    session.insertAll(teacherCourseEntity, list);
  }

  private void createTeacherCourseReportView(String viewName, String teacherEntityName, String teacherCourseEntityName) {
    session.createView(viewName, teacherEntityName, query -> query
      .setProjection(projection ->
        projection.addField("teacher_id", field(teacherEntityName + ".id"))
          .addField("teacher_name", field("name"))
          .addField("age_max", max(field("age")))
          .addField("age_count", count(field("age")))
      )
      .setJoins(joiners -> joiners
        .addLeftJoin(joiner -> joiner
          .setFrom(teacherCourseEntityName)
        )
      )
      .setFilter(f -> f.greaterThanOrEqualTo(teacherEntityName + ".id", 1))
      .setGroupBy(groupBy -> groupBy
        .addField(teacherEntityName + ".id")
        .addField("teacher_name")
      )
      .setSort(sort -> sort.addOrder(teacherEntityName + ".id", Direction.DESC))
      .setPage(1,100)
    );
  }

  @Test
  void testInsert() {
    String entityName = "testInsert_teacher";
    createTeacherEntity2(entityName);
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
    createTeacherEntity2(entityName);
  }

  @Test
  void testUpdate() {
    String entityName = "testUpdate_teacher";
    createTeacherEntity2(entityName);
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
    createTeacherEntity2(entityName);
    Map<String, Object> record = session.findById(entityName, 1);
    Assertions.assertFalse(record.isEmpty());
    Map<String, Object> record2 = session.findById(entityName, 999);
    Assertions.assertNull(record2);
  }

  @Test
  void testFind() {
    String entityName = "testFind_teacher";
    createTeacherEntity2(entityName);
    List<Map<String, Object>> list = session.find(entityName, query ->
      query
        .setProjection(project -> project
          .addField("teacher_id", field("id"))
          .addField("teacher_name", field("name"))
        )
        .setFilter(f -> f.or(or -> or.equalTo("name", "张三").equalTo("name", "李四"))));
    Assertions.assertFalse(list.isEmpty());
    Assertions.assertEquals(2, list.size());
  }

  @Test
  void testFindByQueryConditionWhenIsView() {
    String entityName = "testFindByQueryConditionWhenIsView_teacher";
    String courseEntityName = "testFindByQueryConditionWhenIsView_teacher_courses";
    String teacherCourseReportViewName = "testFindByQueryConditionWhenIsView_teacher_course_report";
    createTeacherEntity2(entityName);
    createTeacherCourseEntity(entityName, courseEntityName);
    createTeacherCourseReportView(teacherCourseReportViewName, entityName, courseEntityName);
    List<Map<String, Object>> list = session.find(teacherCourseReportViewName, query ->
      query.setFilter(f -> f.equalTo("teacher_id", 2)));
    Assertions.assertFalse(list.isEmpty());
    Map<String, Object> item = list.getFirst();
    Assertions.assertEquals(2L, ((Number) item.get("teacher_id")).intValue());
    Assertions.assertEquals("李四", item.get("teacher_name"));
    Assertions.assertEquals(37, ((Number) item.get("age_max")).intValue());
    Assertions.assertEquals(4, ((Number) item.get("age_count")).intValue());
  }

  @Test
  void testFindByQueryConditionWhenHasGroupBy() {
    String entityName = "testFindByQueryConditionWhenHasGroupBy_teacher";
    String courseEntityName = "testFindByQueryConditionWhenHasGroupBy_teacher_courses";
    createTeacherEntity2(entityName);
    createTeacherCourseEntity(entityName, courseEntityName);
    // 聚合分组
    List<Map<String, Object>> groupList = session.find(entityName, query -> query
      .setProjection(projection -> projection
        .addField("teacher_name", field("name"))
        .addField("course_count", count(field(courseEntityName + ".teacher_id")))
        .addField("course_score_sum", sum(field(courseEntityName + ".c_score")))
      )
      .setJoins(joiners -> joiners
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
      .setGroupBy(groupBy -> groupBy
        .addField("teacher_name")
      )
      .setFilter(f -> f.equalTo("name", "李四"))
    );
    Assertions.assertFalse(groupList.isEmpty());
    Map<String, Object> groupFirst = groupList.getFirst();
    Assertions.assertEquals("李四", groupFirst.get("teacher_name"));
    Assertions.assertEquals(4, ((Number) groupFirst.get("course_count")).intValue());
    Assertions.assertEquals(338, ((Number) groupFirst.get("course_score_sum")).intValue());
    // group by time
    List<Map<String, Object>> dateFormatList = session.find(entityName, query -> query
      .setProjection(projection -> projection
        .addField("year", dateFormat(field("birthday"), "yyyy-MM-dd hh:mm:ss"))
        .addField("user_count", count(field("id"))))
      .setGroupBy(groupBy ->
        groupBy.addField("year")
      )
    );
    Assertions.assertFalse(dateFormatList.isEmpty());
    List<Map<String, Object>> dateFormatList1 = session.find(entityName, query -> query
      .setProjection(projection -> projection
        .addField("year", dateFormat(field("birthday"), "yyyy-MM-dd"))
        .addField("user_count", count(field("id"))))
      .setGroupBy(groupBy ->
        groupBy.addField("year")
      )
    );
    Assertions.assertFalse(dateFormatList1.isEmpty());
    List<Map<String, Object>> dateFormatList2 = session.find(entityName, query -> query
      .setProjection(projection -> projection
        .addField("year", dateFormat(field("createDatetime"), "yyyy-MM-dd hh:mm:ss"))
        .addField("user_count", count(field("id"))))
      .setGroupBy(groupBy ->
        groupBy.addField("year")
      )
    );
    Assertions.assertFalse(dateFormatList2.isEmpty());
    List<Map<String, Object>> dayOfYearList = session.find(entityName, query -> query
      .setProjection(projection -> projection
        .addField("dayOfYear", dayOfYear(field("birthday")))
        .addField("user_count", count(field("id"))))
      .setGroupBy(groupBy ->
        groupBy.addField("dayOfYear")
      )
    );
    Assertions.assertFalse(dayOfYearList.isEmpty());
    List<Map<String, Object>> dayOfMonthList = session.find(entityName, query -> query
      .setProjection(projection -> projection
        .addField("dayOfMonth", dayOfMonth(field("birthday")))
        .addField("user_count", count(field("id"))))
      .setGroupBy(groupBy ->
        groupBy.addField("dayOfMonth")
      )
    );
    Assertions.assertFalse(dayOfMonthList.isEmpty());
    List<Map<String, Object>> dayOfWeekList = session.find(entityName, query -> query
      .setProjection(projection -> projection
        .addField("dayOfWeek", dayOfWeek(field("birthday")))
        .addField("user_count", count(field("id"))))
      .setGroupBy(groupBy ->
        groupBy.addField("dayOfWeek")
      )
    );
    Assertions.assertFalse(dayOfWeekList.isEmpty());
  }

  @Test
  void testCountByCondition() {
    String entityName = "testCountByCondition_teacher";
    createTeacherEntity2(entityName);
    long total = session.count(entityName, query -> query
      .setFilter(f -> f.or(or -> or.equalTo("name", "张三").equalTo("name", "李四"))));
    Assertions.assertEquals(2, total);
  }

  @Test
  void testExistsByCondition() {
    String entityName = "testExistsByCondition_teacher";
    createTeacherEntity2(entityName);
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
    createTeacherEntity2(entityName);
    List<Map<String, Object>> list = session.find(entityName, query -> query);
    Assertions.assertFalse(list.isEmpty());
  }

  @Test
  void testFindAllWhenResultDto() {
    String entityName = "testFindAllWhenResultDto_teacher";
    createTeacherEntity2(entityName);
    List<TeacherDTO> list = session.find(entityName, query -> query, TeacherDTO.class);
    Assertions.assertFalse(list.isEmpty());
    Assertions.assertNotNull(list.getFirst().getId());
  }

  @Test
  void testCountAll() {
    String entityName = "testCountAll_teacher";
    createTeacherEntity2(entityName);
    Assertions.assertTrue(session.exists(entityName, query -> query));
  }

  @Test
  void testExistsById() {
    String entityName = "testExistsById_teacher";
    createTeacherEntity2(entityName);
    Assertions.assertTrue(session.existsById(entityName, 1));
  }

  @Test
  void testDeleteById() {
    String entityName = "testDeleteById_teacher";
    createTeacherEntity2(entityName);
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
    createTeacherEntity2(entityName);
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
    createTeacherEntity2(entityName);
    int affectedRows = session.deleteAll(entityName);
    Assertions.assertTrue(affectedRows > 0);
  }

  @Test
  void testQueryNoProjection() {
    String entityName = "testRelation_teacher";
    String courseEntityName = "testRelation_teacher_courses";
    createTeacherEntity2(entityName);
    createTeacherCourseEntity(entityName, courseEntityName);
    List<Map<String, Object>> list = session.find(entityName, query -> query
      .setProjection(projection -> projection
        .addField("teacher_name", field("name")))
      .setSort(sort -> sort.addOrder("id")
      ));
    Assertions.assertFalse(list.isEmpty());
    Map<String, Object> first = list.getFirst();
    Assertions.assertEquals("张三", first.get("teacher_name"));
  }


  void createStudentEntity2(String entityName) {
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
  void testCreateEntity() {
    createStudentEntity2("testCreateEntity_holiday");
  }

  @Test
  void testDropEntity() {
    String entityName = "testDropEntity_holiday";
    createStudentEntity2(entityName);
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
    Assertions.assertEquals(4, ((Entity) session.getModel(classesEntityName)).getFields().size());
    session.modifyField(new StringField("classCode").setModelName(classesEntityName).setLength(100));
    Assertions.assertEquals(4, ((Entity) session.getModel(classesEntityName)).getFields().size());
    session.modifyField(new TextField("className").setModelName(classesEntityName));
    Assertions.assertEquals(4, ((Entity) session.getModel(classesEntityName)).getFields().size());
  }

  @Test
  void testCreateField() {
    String entityName = "testCreateField_students";
    createStudentEntity2(entityName);
    dropModel(entityName);
  }

  @Test
  void testDropField() {
    String entityName = "testDropField_students";
    createStudentEntity2(entityName);
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
    createStudentEntity2(entityName);
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
  void testCreateView() {
    String viewName = "testCreateView_student_score_report";
    String studentModelName = "testCreateView_students";
    String scoreModelName = "testCreateView_student_scores";
    createStudentEntity2(studentModelName);
    createScoreEntity2(scoreModelName);
    createScoreReportView(viewName, studentModelName, scoreModelName);
  }

  private void createScoreReportView(String viewName, String studentModelName, String scoreModelName) {
    session.createView(viewName, studentModelName, query ->
      query.setProjection(projection -> projection
          .addField("student_id", field("id"))
          .addField("student_name", max(field("name")))
          .addField("score_sum", sum(field(scoreModelName + ".score")))
          .addField("score_avg", avg(field(scoreModelName + ".score")))
          .addField("course_count", count(field(scoreModelName + ".course_name")))
        )
        .setJoins(joiners -> joiners
          .addInnerJoin(joiner -> joiner.setFrom(scoreModelName).setLocalField("id").setForeignField("student_id"))
        )
        .setFilter("""
          {
            "id": {"_ne": 999}
          }
          """)
        .setGroupBy(groupBy ->
          groupBy.addField("id")
        )
        .setSort(sort -> sort
          .addOrder("id", DESC)
        )
        .setPage(1,100)
    );
  }

  @Test
  void testDropView() {
    String viewName = "testDropView_student_score_report";
    String studentModelName = "testDropView_students";
    String scoreModelName = "testDropView_student_scores";
    createStudentEntity2(studentModelName);
    createScoreEntity2(scoreModelName);
    createScoreReportView(viewName, studentModelName, scoreModelName);
    dropModel(viewName);
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
    createStudentEntity(studentEntityName);
    createStudentDetailEntity(studentDetailEntityName);
    createCourseEntity(courseEntityName);
    createTeacherEntity(teacherEntityName);
    createAssociations(classesEntityName, studentEntityName, studentDetailEntityName, courseEntityName, teacherEntityName);
    createCourseData(courseEntityName);
    createClassesData(classesEntityName);
    createStudentData(studentEntityName);
    createTeacherData(teacherEntityName);
    createTeacherEntity2("TestSyncModelsteacher2");
    DataSourceProvider dataSourceProvider = sessionFactory.getDataSourceProvider("system");
    String identifier = "sync_test";
    sessionFactory.addDataSourceProvider(identifier, dataSourceProvider);
    if (dataSourceProvider instanceof JdbcDataSourceProvider jdbcDataSourceProvider) {
      SessionFactory sessionFactory = SessionFactory.builder()
        .setDefaultDataSourceProvider(jdbcDataSourceProvider)
        .build();
      sessionFactory.addDataSourceProvider(identifier, dataSourceProvider);
      Session newSession = sessionFactory.createSession(identifier);
      List<Model> models = newSession.syncModels();
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
    sessionFactory.loadScript("default", "import2.json");
  }

}
