package tech.wetech.flexmodel.graphql;

import graphql.ExecutionResult;
import graphql.GraphQL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static tech.wetech.flexmodel.graphql.Models.*;

/**
 * @author cjbi
 */
public class GraphQLProviderTest extends AbstractIntegrationTest {

  @Test
  void testFind() {
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
        classes: system_testSimpleQueryClasses_list(page: 1, size:1) {
          id, students { name: studentName, courses { courseName } }
        }
        students: system_testSimpleQueryStudent_list(
          size: 3
          page: 1
          order_by: {classId: asc, id: desc}
        ) {
          id, studentName
        }
        teachers: system_testSimpleQueryTeacher_list {
         id, teacherName
        }
        course: system_testSimpleQueryCourse {
           courseNo, courseName
        }
      }
      """;
    ExecutionResult executionResult = graphQL.execute(query);
    Map<String, Object> data = executionResult.getData();
    // 打印结果
    System.out.println(executionResult);
    Assertions.assertNotNull(data);
    Assertions.assertNotNull(data.get("course"));
  }

  @Test
  void testMutation() {
    String classesEntityName = "testMutationClasses";
    String studentEntityName = "testMutationStudent";
    String studentDetailEntityName = "testMutationStudentDetail";
    String courseEntityName = "testMutationCourse";
    String teacherEntityName = "testMutationTeacher";
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
      mutation {
        class: system_create_testMutationClasses(data:{className: "测试班级", classCode: "TestC"}) {
          classCode
        }
        course: system_create_testMutationCourse(data: {courseName: "测试课程", courseNo: "Test_CC"}) {
            courseName
            courseNo
        }
        student: system_create_testMutationStudent(data: {studentName: "张三丰", gender: "男", age: 200, classId: 1, remark: {test:"aa"}}) {
            id
            remark
        }
      }
      """;
    ExecutionResult executionResult = graphQL.execute(query);
    log.info("query result: {}", executionResult);
    Map<String, Object> data = executionResult.getData();
    Assertions.assertNotNull(data.get("class"));
    Assertions.assertNotNull(data.get("course"));
    Assertions.assertNotNull(data.get("student"));
    String query2 = """
      mutation MyMutation($studentId: ID!, $courseNo: ID!) {
        class: system_delete_testMutationCourse_by_id(id: $courseNo) {
          courseNo
        }
        student: system_update_testMutationStudent_by_id(_set: {age: 199, remark: {test: "bb"}}, id: $studentId) {
          id
        }
      }
      """;
    Map<String, Object> variables = new HashMap<>();
    if (data.get("student") instanceof Map<?, ?> map) {
      variables.put("studentId", map.get("id"));
    }
    variables.put("courseNo", "Test_CC");
    ExecutionResult executionResult2 = graphQL.execute(i -> i.query(query2).variables(variables));
    log.info("query result: {}", executionResult2);
    Map<String, Object> data2 = executionResult2.getData();
    Assertions.assertNotNull(data2.get("class"));
  }

}
