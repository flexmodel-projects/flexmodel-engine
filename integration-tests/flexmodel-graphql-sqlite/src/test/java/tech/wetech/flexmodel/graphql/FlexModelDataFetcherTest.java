package tech.wetech.flexmodel.graphql;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.*;

import java.util.List;
import java.util.Map;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static tech.wetech.flexmodel.IDField.GeneratedValue.*;
import static tech.wetech.flexmodel.RelationField.Cardinality.*;

/**
 * @author cjbi
 */

public class FlexModelDataFetcherTest extends AbstractIntegrationTest {

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
      .addField(new IDField("id").setGeneratedValue(AUTO_INCREMENT))
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
      new RelationField("students")
        .setTargetEntity(studentEntityName)
        .setTargetField("classId")
        .setCardinality(ONE_TO_MANY)
        .setCascadeDelete(true)
    );
    // 学生:课程 -> n:n
    session.createField(
      studentEntityName,
      new RelationField("courses")
        .setTargetEntity(courseEntityName)
        .setTargetField("courseNo")
        .setCardinality(MANY_TO_MANY)
    );
    // 学生:学生明细 -> 1:1
    session.createField(studentEntityName,
      new RelationField("studentDetail")
        .setTargetEntity(studentDetailEntityName)
        .setTargetField("studentId")
        .setCardinality(ONE_TO_ONE)
    );
    // 学生:教师 -> n:n
    session.createField(
      studentEntityName,
      new RelationField("teachers")
        .setTargetEntity(teacherEntityName)
        .setTargetField("id")
        .setCardinality(MANY_TO_MANY)
    );
    // 教师:学生 -> n:n
    session.createField(
      teacherEntityName,
      new RelationField("students")
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
  void testFirst() {
    String classesEntityName = "TestFirstClasses";
    String studentEntityName = "TestFirstStudent";
    String studentDetailEntityName = "TestFirstStudentDetail";
    String courseEntityName = "TestFirstCourse";
    String teacherEntityName = "TestFirstTeacher";
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
//    createTeacherCourseEntity(teacherEntity, teacherCourseEntity);
//    List<Model> allModels = session.getAllModels();
//    GraphQLObjectType testFirstTeacherType = newObject()
//      .name("testFirst_teacher")
//      .field(
//        newFieldDefinition()
//
//      )
//      .build();
    String schema = """
       type Query {
         TestFirstClasses : [TestFirstClasses]
         TestFirstStudent : [TestFirstStudent]
         TestFirstStudentDetail : [TestFirstStudentDetail]
         TestFirstCourse: [TestFirstCourse]
         TestFirstTeacher: [TestFirstTeacher]
       }
       type TestFirstClasses {
         id : ID
         classCode : String
         className : String
         students: [TestFirstStudent]
       }
       type TestFirstStudent {
          id : ID
          studentName: String
          gender: String
          age: Int
          classId: Int
          studentDetail: TestFirstStudentDetail
          courses: [TestFirstCourse]
          teachers: [TestFirstTeacher]
       }
       type TestFirstStudentDetail {
         id: ID
         studentId: Int
         description: String
       }
       type TestFirstCourse {
         courseNo: ID
         courseName: String
       }
       type TestFirstTeacher {
         id: ID
         teacherName: String
         subject: String
         students: TestFirstStudent
       }
      """;
    SchemaParser schemaParser = new SchemaParser();
    TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);
    RuntimeWiring runtimeWiring = newRuntimeWiring()
      .type("Query", builder -> builder.defaultDataFetcher(new FlexModelDataFetcher(session)))
      .build();
    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
    GraphQL graphQL = GraphQL.newGraphQL(graphQLSchema).build();
    ExecutionResult result = graphQL.execute("""
      query {
       TestFirstClasses {
         id
         classCode
         className
         students {
           id
           studentName
           gender
           age
           studentDetail {
             description
           }
           courses {
             courseNo
             courseName
           }
           teachers {
             id
             teacherName
           }
         }
       }
      }
      """);
    System.out.println(JsonUtils.getInstance().stringify(result.getData()));
    Assertions.assertNotNull(result.getData());
  }

}
