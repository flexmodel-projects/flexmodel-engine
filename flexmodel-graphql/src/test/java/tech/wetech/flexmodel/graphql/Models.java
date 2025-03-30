package tech.wetech.flexmodel.graphql;

import tech.wetech.flexmodel.Enum;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter;

import java.util.List;
import java.util.Map;

import static tech.wetech.flexmodel.IDField.GeneratedValue.*;

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
      .addField(new JSONField("description"))
    );
  }

  public static Entity createStudentEntity(Session session, String entityName) {
    Enum genderEnum = session.createEnum(entityName + "_gender", en ->
      en.addElement("UNKNOWN")
        .addElement("MALE")
        .addElement("FEMALE")
        .setComment("性别")
    );
    Enum interestEnum = session.createEnum(entityName + "_interest", en ->
      en.addElement("chang")
        .addElement("tiao")
        .addElement("rap")
        .addElement("daLanQiu")
        .setComment("兴趣")
    );
    return session.createEntity(entityName, entity -> entity
      .addField(new IDField("id").setGeneratedValue(BIGINT_NOT_GENERATED))
      .addField(new StringField("studentName"))
      .addField(new EnumField("gender").setFrom(genderEnum.getName()))
      .addField(new EnumField("interest").setFrom(interestEnum.getName()).setMultiple(true))
      .addField(new IntField("age"))
      .addField(new IntField("classId"))
      .addField(new JSONField("remark"))
    );
  }

  public static Entity createStudentDetailEntity(Session session, String entityName) {
    return session.createEntity(entityName, entity -> entity
      .addField(new IDField("id").setGeneratedValue(AUTO_INCREMENT))
      .addField(new LongField("studentId"))
      .addField(new StringField("description"))
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
      .setFrom(studentEntityName)
      .setForeignField("classId")
      .setMultiple(true)
      .setCascadeDelete(true)
    );
    // 学生:学生明细 -> 1:1
    session.createField(new RelationField("studentDetail")
      .setModelName(studentEntityName)
      .setFrom(studentDetailEntityName)
      .setForeignField("studentId")
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
          "gender": "MALE",
          "age": 10,
          "classId": 1,
          "remark": {
              "foo": "bar"
          },
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
          "age": 10,
          "classId": 1,
          "remark": {
              "leader": "John Doe"
          },
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
          "gender": "MALE",
          "age": 11,
          "classId": 2,
          "remark": null,
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
