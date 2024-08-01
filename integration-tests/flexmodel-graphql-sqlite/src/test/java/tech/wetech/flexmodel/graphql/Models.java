package tech.wetech.flexmodel.graphql;

import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter;

import java.util.List;
import java.util.Map;

import static tech.wetech.flexmodel.IDField.GeneratedValue.*;
import static tech.wetech.flexmodel.RelationField.Cardinality.*;

/**
 * @author cjbi
 */
public class Models {

  private static final JsonObjectConverter jsonObjectConverter = new JacksonObjectConverter();

  public static Entity createClassesEntity(Session session, String entityName) {
    return session.createEntity(entityName, entity -> entity
      .addField(new IDField("id").setGeneratedValue(BIGINT_NOT_GENERATED))
      .addField(new StringField("classCode"))
      .addField(new StringField("className"))
    );
  }

  public static Entity createStudentEntity(Session session, String entityName) {
    return session.createEntity(entityName, entity -> entity
      .addField(new IDField("id").setGeneratedValue(BIGINT_NOT_GENERATED))
      .addField(new StringField("studentName"))
      .addField(new StringField("gender"))
      .addField(new IntField("age"))
      .addField(new IntField("classId"))
    );
  }

  public static Entity createStudentDetailEntity(Session session, String entityName) {
    return session.createEntity(entityName, entity -> entity
      .addField(new IDField("id").setGeneratedValue(AUTO_INCREMENT))
      .addField(new IntField("studentId"))
      .addField(new TextField("description"))
    );
  }

  public static Entity createCourseEntity(Session session, String entityName) {
    return session.createEntity(entityName, entity -> entity
      .addField(new IDField("courseNo").setGeneratedValue(STRING_NOT_GENERATED))
      .addField(new StringField("courseName"))
    );
  }

  public static Entity createTeacherEntity(Session session, String entityName) {
    return session.createEntity(entityName, entity -> entity
      .addField(new IDField("id").setGeneratedValue(BIGINT_NOT_GENERATED))
      .addField(new StringField("teacherName"))
      .addField(new StringField("subject"))
    );
  }

  public static void createAssociations(Session session, String classRoomEntityName, String studentEntityName,
                                        String studentDetailEntityName, String courseEntityName, String teacherEntityName) {
    // 班级:学生
    session.createField(new RelationField("students")
      .setModelName(classRoomEntityName)
      .setTargetEntity(studentEntityName)
      .setTargetField("classId")
      .setCardinality(ONE_TO_MANY)
      .setCascadeDelete(true)
    );
    // 学生:课程 -> n:n
    session.createField(new RelationField("courses")
      .setModelName(studentEntityName)
      .setTargetEntity(courseEntityName)
      .setTargetField("courseNo")
      .setCardinality(MANY_TO_MANY)
    );
    // 学生:学生明细 -> 1:1
    session.createField(new RelationField("studentDetail")
      .setModelName(studentEntityName)
      .setTargetEntity(studentDetailEntityName)
      .setTargetField("studentId")
      .setCardinality(ONE_TO_ONE)
    );
    // 学生:教师 -> n:n
    session.createField(new RelationField("teachers")
      .setModelName(studentEntityName)
      .setTargetEntity(teacherEntityName)
      .setTargetField("id")
      .setCardinality(MANY_TO_MANY)
    );
    // 教师:学生 -> n:n
    session.createField(new RelationField("students")
      .setModelName(courseEntityName)
      .setTargetEntity(studentEntityName)
      .setTargetField("id")
      .setCardinality(MANY_TO_MANY)
    );
  }

  public static void createClassesData(Session session, String entityName) {
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
    List<Map<String, Object>> list = jsonObjectConverter.parseToObject(mockData, List.class);
    session.insertAll(entityName, list);
  }

  public static void createStudentData(Session session, String entityName) {
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
    List<Map<String, Object>> list = jsonObjectConverter.parseToObject(mockData, List.class);
    session.insertAll(entityName, list);
  }

  public static void createCourseData(Session session, String entityName) {
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
    List<Map<String, Object>> list = jsonObjectConverter.parseToObject(mockData, List.class);
    session.insertAll(entityName, list);
  }

  public static void createTeacherData(Session session, String entityName) {
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
    List<Map<String, Object>> list = jsonObjectConverter.parseToObject(mockData, List.class);
    session.insertAll(entityName, list);
  }

}
