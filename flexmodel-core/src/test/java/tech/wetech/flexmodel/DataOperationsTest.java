package tech.wetech.flexmodel;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.calculations.DatetimeNowValueCalculator;
import tech.wetech.flexmodel.dto.TeacherDTO;
import tech.wetech.flexmodel.validations.NumberRangeValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tech.wetech.flexmodel.AssociationField.Cardinality.ONE_TO_MANY;
import static tech.wetech.flexmodel.Projections.*;

/**
 * @author cjbi
 */
@SuppressWarnings("unchecked")
public class DataOperationsTest extends AbstractSessionIntegrationTest {

  void createTeacherEntity(String entityName) {
    session.createEntity(entityName, entity -> entity
      // 主键
      .addField(new IDField("id").setGeneratedValue(IDField.DefaultGeneratedValue.IDENTITY).setComment("Primary Key"))
      // 姓名
      .addField(new StringField("name").setComment("姓名").setNullable(false).setLength(10))
      // 年龄
      .addField(new IntField("age").setComment("年龄").addValidation(new NumberRangeValidator<>(1, 300)))
      // 备注
      .addField(new TextField("description").setComment("备注"))
      // 生日
      .addField(new DateField("birthday").setComment("生日"))
      // 是否禁用
      .addField(new BooleanField("isLocked").setNullable(false).setDefaultValue(false).setComment("是否禁用"))
      // 创建时间
      .addField(new DatetimeField("createDatetime").setComment("创建日期时间").addCalculation(new DatetimeNowValueCalculator()))
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
          "createDatetime": "2024-04-24T19:30:41.0035688",
          "name": "张三",
          "description": "软件工程师",
          "age": 25
        },
        {
          "birthday": "1995-03-28",
          "isLocked": true,
          "createDatetime": "2024-04-24T19:30:41.0035688",
          "name": "李四",
          "description": "市场营销经理",
          "age": 37
        },
        {
          "birthday": "1991-01-12",
          "isLocked": false,
          "createDatetime": "2024-04-24T19:30:41.0035688",
          "name": "王五",
          "description": "人力资源专员",
          "age": 42
        },
        {
          "birthday": "1965-01-07",
          "isLocked": true,
          "createDatetime": "2024-04-24T19:30:41.0035688",
          "name": "赵六",
          "description": "退休教师",
          "age": 55
        },
        {
          "birthday": "1991-10-23",
          "isLocked": false,
          "createDatetime": "2024-04-24T19:30:41.0035688",
          "name": "孙七",
          "description": "设计师",
          "age": 29
        },
        {
          "birthday": "1995-05-01",
          "isLocked": true,
          "createDatetime": "2024-04-24T19:30:41.0035688",
          "name": "周八",
          "description": "产品经理",
          "age": 32
        },
        {
          "birthday": "1991-10-20",
          "isLocked": false,
          "createDatetime": "2024-04-24T19:30:41.0035688",
          "name": "吴九",
          "description": "会计",
          "age": 45
        }
      ]
      """;
    List<Map<String, Object>> list = JsonUtils.getInstance().parseToObject(mockData, List.class);
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
    List<Map<String, Object>> list = JsonUtils.getInstance().parseToObject(mockData, List.class);
    session.createEntity(teacherCourseEntity, sScore -> sScore
      .addField(new IDField("id"))
      .addField(new StringField("c_name"))
      .addField(new DecimalField("c_score"))
      .addField(new BigintField("teacher_id"))
      .setComment("教师成绩表")
    );
    session.createField(teacherEntityName,
      new AssociationField("courses")
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
      .setFilter(String.format("""
        {
          ">=": [{ "var": ["%s.id"] }, 1]
        }
        """, teacherEntityName))
      .setGroupBy(groupBy -> groupBy
        .addField(teacherEntityName + ".id")
        .addField("teacher_name")
      )
      .setSort(sort -> sort.addOrder(teacherEntityName + ".id", Direction.DESC))
      .setLimit(100)
      .setOffset(0)
    );
  }

  @Test
  void testInsert() {
    String entityName = "testInsert_teacher";
    createTeacherEntity(entityName);
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
    createTeacherEntity(entityName);
  }

  @Test
  void testUpdate() {
    String entityName = "testUpdate_teacher";
    createTeacherEntity(entityName);
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
    createTeacherEntity(entityName);
    Map<String, Object> record = session.findById(entityName, 1);
    Assertions.assertFalse(record.isEmpty());
    Map<String, Object> record2 = session.findById(entityName, 999);
    Assertions.assertNull(record2);
  }

  @Test
  void testFind() {
    String entityName = "testFind_teacher";
    createTeacherEntity(entityName);
    List<Map<String, Object>> list = session.find(entityName, query ->
      query
        .setProjection(project -> project
          .addField("teacher_id", field("id"))
          .addField("teacher_name", field("name"))
        )
        .setFilter("""
          {
            "or": [
              {
                "==": [{ "var": ["name"] }, "张三"]
              },
              {
                "==": [{ "var": ["name"] }, "李四"]
              }
            ]
          }
          """));
    Assertions.assertFalse(list.isEmpty());
    Assertions.assertEquals(2, list.size());
  }

  @Test
  void testFindByQueryConditionWhenIsView() {
    String entityName = "testFindByQueryConditionWhenIsView_teacher";
    String courseEntityName = "testFindByQueryConditionWhenIsView_teacher_courses";
    String teacherCourseReportViewName = "testFindByQueryConditionWhenIsView_teacher_course_report";
    createTeacherEntity(entityName);
    createTeacherCourseEntity(entityName, courseEntityName);
    createTeacherCourseReportView(teacherCourseReportViewName, entityName, courseEntityName);
    List<Map<String, Object>> list = session.find(teacherCourseReportViewName, query -> query.setFilter("""
      {
        "==": [{ "var": ["teacher_id"] }, 2]
      }
      """));
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
    createTeacherEntity(entityName);
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
              "!=": [{ "var": ["teacher_id"] }, 999]
            }
            """)
        )
      )
      .setGroupBy(groupBy -> groupBy
        .addField("teacher_name")
      )
      .setFilter("""
        {
          "==": [{ "var": ["name"] }, "李四"]
        }
        """)
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
    createTeacherEntity(entityName);
    long total = session.count(entityName, query -> query.setFilter("""
      {
        "or": [
          {
            "==": [{ "var": ["name"] }, "张三"]
          },
          {
            "==": [{ "var": ["name"] }, "李四"]
          }
        ]
      }
      """));
    Assertions.assertEquals(2, total);
  }

  @Test
  void testExistsByCondition() {
    String entityName = "testExistsByCondition_teacher";
    createTeacherEntity(entityName);
    boolean exists = session.exists(entityName, query -> query.setFilter("""
      {
        "or": [
          {
            "==": [{ "var": ["name"] }, "张三"]
          },
          {
            "==": [{ "var": ["name"] }, "李四"]
          }
        ]
      }
      """));
    Assertions.assertTrue(exists);
  }

  @Test
  void testFindAll() {
    String entityName = "testFindAll_teacher";
    createTeacherEntity(entityName);
    List<Map<String, Object>> list = session.find(entityName, query -> query);
    Assertions.assertFalse(list.isEmpty());
  }

  @Test
  void testFindAllWhenResultDto() {
    String entityName = "testFindAllWhenResultDto_teacher";
    createTeacherEntity(entityName);
    List<TeacherDTO> list = session.find(entityName, query -> query, TeacherDTO.class);
    Assertions.assertFalse(list.isEmpty());
  }

  @Test
  void testCountAll() {
    String entityName = "testCountAll_teacher";
    createTeacherEntity(entityName);
    Assertions.assertTrue(session.exists(entityName, query -> query));
  }

  @Test
  void testExistsById() {
    String entityName = "testExistsById_teacher";
    createTeacherEntity(entityName);
    Assertions.assertTrue(session.existsById(entityName, 1));
  }

  @Test
  void testDeleteById() {
    String entityName = "testDeleteById_teacher";
    createTeacherEntity(entityName);
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
    createTeacherEntity(entityName);
    int affectedRows = session.delete(entityName, """
      {
        "==": [{ "var": ["id"] }, 999]
      }
      """);
    Assertions.assertEquals(0, affectedRows);
  }

  @Test
  void testDeleteAll() {
    String entityName = "testDeleteAll_teacher";
    createTeacherEntity(entityName);
    int affectedRows = session.deleteAll(entityName);
    Assertions.assertTrue(affectedRows > 0);
  }

  @Test
  void testQueryNoProjection() {
    String entityName = "testRelation_teacher";
    String courseEntityName = "testRelation_teacher_courses";
    createTeacherEntity(entityName);
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

  @Test
  void testRelation() {

  }
}
