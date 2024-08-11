package tech.wetech.flexmodel.graphql;

import graphql.ExecutionResult;
import graphql.GraphQL;
import org.junit.jupiter.api.Test;

import static tech.wetech.flexmodel.graphql.Models.*;

/**
 * @author cjbi
 */
public class GraphQLProviderTest extends AbstractIntegrationTest {

//  @Test
//  void testTransform() {
//    GraphQLProvider graphQLProvider = new GraphQLProvider(session.getFactory());
//    graphQLProvider.init();
//    String classesEntityName = "testTransformClasses";
//    String studentEntityName = "testTransformStudent";
//    String studentDetailEntityName = "testTransformStudentDetail";
//    String courseEntityName = "testTransformCourse";
//    String teacherEntityName = "testTransformTeacher";
//    createClassesEntity(session, classesEntityName);
//    Entity studentEntity = createStudentEntity(session, studentEntityName);
//    Entity studentDetailEntity = createStudentDetailEntity(session, studentDetailEntityName);
//    Entity courseEntity = createCourseEntity(session, courseEntityName);
//    Entity teacherEntity = createTeacherEntity(session, teacherEntityName);
//    createAssociations(session, classesEntityName, studentEntityName, studentDetailEntityName, courseEntityName, teacherEntityName);
//
//    graphQLProvider.transformInsert(session.getName(), List.of(studentDetailEntity));
//    createCourseData(session, courseEntityName);
//    createClassesData(session, classesEntityName);
//    createStudentData(session, studentEntityName);
//    createTeacherData(session, teacherEntityName);
//
//    GraphQL graphQL = graphQLProvider.getGraphQL();
//    // 创建查询
//    String query = """
//      query {
//        classes: system_testTransformClasses {
//          id, students { name: studentName, courses { courseName } }
//        }
//        students: system_testTransformStudent {
//          id, studentName, studentDetail { description }
//        }
//        teachers: system_testTransformTeacher {
//         id, teacherName
//        }
//      }
//      """;
//    ExecutionResult executionResult = graphQL.execute(query);
//    // 打印结果
//    System.out.println(executionResult);
//    Assertions.assertTrue(executionResult.getErrors().isEmpty());
//    Assertions.assertNotNull(executionResult.getData());
//    graphQLProvider.transformDelete(session.getName(), List.of(studentEntity));
//  }

  @Test
  void testSimpleQuery() throws Exception {
    String classesEntityName = "testSimpleQueryClasses";
    String studentEntityName = "testSimpleQueryStudent";
    String studentDetailEntityName = "testSimpleQueryStudentDetail";
    String courseEntityName = "testSimpleQueryCourse";
    String teacherEntityName = "testSimpleQueryTeacher";
    createClassesEntity(session, classesEntityName);
    createStudentEntity(session, studentEntityName);
    createStudentDetailEntity(session, studentDetailEntityName);
    createCourseEntity(session, courseEntityName);
    createTeacherEntity(session, teacherEntityName);
    createAssociations(session, classesEntityName, studentEntityName, studentDetailEntityName, courseEntityName, teacherEntityName);
    createCourseData(session, courseEntityName);
    createClassesData(session, classesEntityName);
    createStudentData(session, studentEntityName);
    createTeacherData(session, teacherEntityName);

    GraphQLProvider graphQLProvider = new GraphQLProvider(session.getFactory());
    graphQLProvider.init();
    GraphQL graphQL = graphQLProvider.getGraphQL();
    // 创建查询
    String query = """
      query {
        classes: find_system_testSimpleQueryClasses {
          id, students { name: studentName, courses { courseName } }
        }
        students: find_system_testSimpleQueryStudent {
          id, studentName, studentDetail { description }
        }
        teachers: find_system_testSimpleQueryTeacher {
         id, teacherName
        }
      }
      """;
    ExecutionResult executionResult = graphQL.execute(query);
    // 打印结果
    System.out.println(executionResult);
  }

}
