package tech.wetech.flexmodel;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static tech.wetech.flexmodel.AssociationField.Cardinality.*;
import static tech.wetech.flexmodel.IDField.DefaultGeneratedValue.*;
import static tech.wetech.flexmodel.Projections.field;

/**
 * @author cjbi
 */
public class DataOperationAssociationTest extends AbstractSessionIntegrationTest {

  void createClassesEntity(String entityName) {
    session.createEntity(entityName, entity -> entity
      .addField(new IDField("id").setGeneratedValue(BIGINT_NO_GEN))
      .addField(new StringField("classCode"))
      .addField(new StringField("className"))
    );
  }

  void createStudentEntity(String entityName) {
    session.createEntity(entityName, entity -> entity
      .addField(new IDField("id").setGeneratedValue(BIGINT_NO_GEN))
      .addField(new StringField("studentName"))
      .addField(new StringField("gender"))
      .addField(new IntField("age"))
      .addField(new IntField("classId"))
    );
  }

  void createStudentDetailEntity(String entityName) {
    session.createEntity(entityName, entity -> entity
      .addField(new IDField("id").setGeneratedValue(IDENTITY))
      .addField(new IntField("studentId"))
      .addField(new TextField("description"))
    );
  }

  void createCourseEntity(String entityName) {
    session.createEntity(entityName, entity -> entity
      .addField(new IDField("courseNo").setGeneratedValue(STRING_NO_GEN))
      .addField(new StringField("courseName"))
    );
  }

  void createTeacherEntity(String entityName) {
    session.createEntity(entityName, entity -> entity
      .addField(new IDField("id").setGeneratedValue(BIGINT_NO_GEN))
      .addField(new StringField("teacherName"))
      .addField(new StringField("subject"))
    );
  }

  void createAssociations(String classRoomEntityName, String studentEntityName,
                          String studentDetailEntityName, String courseEntityName, String teacherEntityName) {
    // 班级:学生
    session.createField(classRoomEntityName,
      new AssociationField("students")
        .setTargetEntity(studentEntityName)
        .setTargetField("classId")
        .setCardinality(ONE_TO_MANY)
        .setCascadeDelete(true)
    );
    // 学生:课程 -> n:n
    session.createField(
      studentEntityName,
      new AssociationField("courses")
        .setTargetEntity(courseEntityName)
        .setTargetField("courseNo")
        .setCardinality(MANY_TO_MANY)
    );
    // 学生:学生明细 -> 1:1
    session.createField(studentEntityName,
      new AssociationField("studentDetail")
        .setTargetEntity(studentDetailEntityName)
        .setTargetField("studentId")
        .setCardinality(ONE_TO_ONE)
    );
    // 学生:教师 -> n:n
    session.createField(
      studentEntityName,
      new AssociationField("teachers")
        .setTargetEntity(teacherEntityName)
        .setTargetField("id")
        .setCardinality(MANY_TO_MANY)
    );
    // 教师:学生 -> n:n
    session.createField(
      teacherEntityName,
      new AssociationField("students")
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
    List<Map<String, Object>> list = JsonUtils.getInstance().parseToObject(mockData, List.class);
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
    List<Map<String, Object>> list = JsonUtils.getInstance().parseToObject(mockData, List.class);
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
    List<Map<String, Object>> list = JsonUtils.getInstance().parseToObject(mockData, List.class);
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
    List<Map<String, Object>> list = JsonUtils.getInstance().parseToObject(mockData, List.class);
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
      .setFilter(
        String.format("""
          {
            "==": [
              {"var": ["%s"]},
              %s
            ]
          }
          """, studentEntityName + ".id", 1)
      )
    ).getFirst();
    Assertions.assertEquals("张三", oneToOne.get("studentName"));
    Assertions.assertEquals("张三的描述", oneToOne.get("description"));

    // 1:n
    List<Map<String, Object>> oneToMany = session.find(classesEntityName, query -> query
      .setProjection(projection -> projection
        .addField("className", field("className"))
        .addField("studentName", field("studentName"))
      )
      .setJoins(joins -> joins
        .addLeftJoin(join -> join
          .setFrom(studentEntityName)
        )
      )
      .setFilter(
        String.format("""
          {
            "==": [
              {"var": ["%s"]},
              %s
            ]
          }
          """, classesEntityName + ".id", 1)
      )
    );
    Assertions.assertFalse(oneToMany.isEmpty());
    Assertions.assertEquals("一年级1班", oneToMany.getFirst().get("className"));
    Assertions.assertEquals("张三", oneToMany.getFirst().get("studentName"));

    // n:n
    List<Map<String, Object>> manyToMany = session.find(studentEntityName, query -> query
      .setProjection(projection -> projection
        .addField("studentName", field("studentName"))
        .addField("teacherName", field("teacherName"))
      )
      .setJoins(joins -> joins.addLeftJoin(join -> join
        .setFrom(teacherEntityName)
      ))
      .setFilter(
        String.format("""
          {
            "==": [
              {"var": ["%s"]},
              %s
            ]
          }
          """, studentEntityName + ".id", 1)
      )
    );
    Assertions.assertFalse(manyToMany.isEmpty());
    Assertions.assertEquals(3, manyToMany.size());
  }

}
