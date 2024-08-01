package tech.wetech.flexmodel.graphql;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.*;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static graphql.Scalars.*;
import static graphql.schema.GraphQLList.list;
import static graphql.schema.GraphQLObjectType.newObject;
import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static tech.wetech.flexmodel.RelationField.Cardinality.ONE_TO_ONE;
import static tech.wetech.flexmodel.graphql.Models.*;

/**
 * @author cjbi
 */

public class FlexmodelDataFetcherTest extends AbstractIntegrationTest {

  private final JsonObjectConverter jsonObjectConverter = new JacksonObjectConverter();

  @Test
  void testFirst() {
    String classesEntityName = "TestFirstClasses";
    String studentEntityName = "TestFirstStudent";
    String studentDetailEntityName = "TestFirstStudentDetail";
    String courseEntityName = "TestFirstCourse";
    String teacherEntityName = "TestFirstTeacher";
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
         system_TestFirstClasses : [system_TestFirstClasses]
         system_TestFirstStudent : [system_TestFirstStudent]
         system_TestFirstStudentDetail : [system_TestFirstStudentDetail]
         system_TestFirstCourse: [system_TestFirstCourse]
         system_TestFirstTeacher: [system_TestFirstTeacher]
       }
       type system_TestFirstClasses {
         id : ID
         classCode : String
         className : String
         students: [system_TestFirstStudent]
       }
       type system_TestFirstStudent {
          id : ID
          studentName: String
          gender: String
          age: Int
          classId: Int
          studentDetail: system_TestFirstStudentDetail
          courses: [system_TestFirstCourse]
          teachers: [system_TestFirstTeacher]
       }
       type system_TestFirstStudentDetail {
         id: ID
         studentId: Int
         description: String
       }
       type system_TestFirstCourse {
         courseNo: ID
         courseName: String
       }
       type system_TestFirstTeacher {
         id: ID
         teacherName: String
         subject: String
         students: system_TestFirstStudent
       }
      """;
    SchemaParser schemaParser = new SchemaParser();
    TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);
    RuntimeWiring runtimeWiring = newRuntimeWiring()
      .type("Query", builder -> builder.defaultDataFetcher(new FlexmodelDataFetcher(SCHEMA_NAME, session.getFactory())))
      .build();
    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
    GraphQL graphQL = GraphQL.newGraphQL(graphQLSchema).build();
    ExecutionResult result = graphQL.execute("""
      query {
       system_TestFirstClasses {
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
    System.out.println(jsonObjectConverter.toJsonString(result.getData()));
    Assertions.assertNotNull(result.getData());
  }

  @Test
  void testCodeRegistry() {
    String classesEntityName = "TestCodeRegistryClasses";
    String studentEntityName = "TestCodeRegistryStudent";
    String studentDetailEntityName = "TestCodeRegistryStudentDetail";
    String courseEntityName = "TestCodeRegistryCourse";
    String teacherEntityName = "TestCodeRegistryTeacher";
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

    // 类型映射关系
    Map<String, GraphQLScalarType> typeMapping = new HashMap<>();
    typeMapping.put(ScalarType.ID.getType(), GraphQLID);
    typeMapping.put(ScalarType.STRING.getType(), GraphQLString);
    typeMapping.put(ScalarType.TEXT.getType(), GraphQLString);
    typeMapping.put(ScalarType.DECIMAL.getType(), GraphQLFloat);
    typeMapping.put(ScalarType.INT.getType(), GraphQLInt);
    typeMapping.put(ScalarType.BIGINT.getType(), GraphQLInt);
    typeMapping.put(ScalarType.BOOLEAN.getType(), GraphQLBoolean);
    typeMapping.put(ScalarType.DATETIME.getType(), GraphQLString);
    typeMapping.put(ScalarType.JSON.getType(), GraphQLString);

    // 注册模型
    List<Model> models = session.getAllModels();
    List<GraphQLObjectType> objectTypes = new ArrayList<>();
    for (Model model : models) {
      if (model instanceof Entity entity && entity.findIdField().isPresent()) {
        List<GraphQLFieldDefinition> fieldDefinitions = new ArrayList<>();
        for (TypedField<?, ?> field : entity.getFields()) {
          if (field instanceof RelationField relationField) {
            GraphQLOutputType relationType;
            if (relationField.getCardinality() == ONE_TO_ONE) {
              relationType = GraphQLTypeReference.typeRef(relationField.getTargetEntity());
            } else {
              relationType = list(GraphQLTypeReference.typeRef(relationField.getTargetEntity()));
            }
            fieldDefinitions.add(
              GraphQLFieldDefinition.newFieldDefinition()
                .name(field.getName())
                .type(relationType)
                .description(field.getComment())
                .build()
            );
          } else {
            fieldDefinitions.add(
              GraphQLFieldDefinition.newFieldDefinition()
                .name(field.getName())
                .type(typeMapping.get(field.getType()))
                .description(field.getComment())
                .build()
            );
          }

        }
        // 注册对象类型
        objectTypes.add(newObject()
          .name(entity.getName())
          .fields(fieldDefinitions)
          .description(entity.getComment())
          .build());
      }
    }
    List<GraphQLFieldDefinition> queryFieldDefinitions = new ArrayList<>();
    for (GraphQLObjectType objectType : objectTypes) {
      queryFieldDefinitions.add(
        GraphQLFieldDefinition.newFieldDefinition()
          .name(objectType.getName())
          .type(list(objectType)).build()
      );
    }

    GraphQLObjectType root = newObject()
      .name("Query")
      .fields(queryFieldDefinitions)
      .build();

    Map<String, DataFetcher<?>> dataFetchers = new HashMap<>();
    for (GraphQLFieldDefinition queryFieldDefinition : queryFieldDefinitions) {
      dataFetchers.put(queryFieldDefinition.getName(), new FlexmodelDataFetcher(SCHEMA_NAME, session.getFactory()));
    }

    // 创建 CodeRegistry
    GraphQLCodeRegistry codeRegistry = GraphQLCodeRegistry
      .newCodeRegistry()
      .dataFetchers("Query", dataFetchers)
      .build();

    // 创建 GraphQL schema
    GraphQLSchema schema = GraphQLSchema.newSchema()
      .query(root)
      .codeRegistry(codeRegistry)
      .build();


    // 执行查询
    GraphQL graphQL = GraphQL.newGraphQL(schema).build();

    // 创建查询
    String query = """
      query {
        classes: TestCodeRegistryClasses {
          id, students { name: studentName, courses { courseName } }
        }
        students: TestCodeRegistryStudent {
          id, studentName, studentDetail { description }
        }
        teachers: TestCodeRegistryTeacher {
         id, teacherName
        }
      }
      """;
    ExecutionResult executionResult = graphQL.execute(query);

    // 打印结果
    System.out.println(jsonObjectConverter.toJsonString(executionResult));
  }

}
