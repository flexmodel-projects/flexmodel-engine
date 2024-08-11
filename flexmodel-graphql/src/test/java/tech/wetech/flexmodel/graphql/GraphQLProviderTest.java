package tech.wetech.flexmodel.graphql;

import graphql.ExecutionResult;
import graphql.GraphQL;
import org.junit.jupiter.api.Test;

import static tech.wetech.flexmodel.graphql.Models.*;

/**
 * @author cjbi
 */
public class GraphQLProviderTest extends AbstractIntegrationTest {

  @Test
  void testFind() throws Exception {
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
