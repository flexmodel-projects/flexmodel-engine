import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.calculations.DatetimeNowValueCalculator;
import tech.wetech.flexmodel.validations.NumberRangeValidator;

import java.util.List;
import java.util.Map;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static tech.wetech.flexmodel.AssociationField.Cardinality.ONE_TO_MANY;

/**
 * @author cjbi
 */

public class RelationTest extends AbstractIntegrationTest {

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

  @Test
  void testFirst() {
    String teacherEntity = "testFirst_teacher";
    String teacherCourseEntity = "testFirst_teacher_course";
    createTeacherEntity("testFirst_teacher");
    createTeacherCourseEntity(teacherEntity, teacherCourseEntity);
    List<Model> allModels = session.getAllModels();
//    GraphQLObjectType testFirstTeacherType = newObject()
//      .name("testFirst_teacher")
//      .field(
//        newFieldDefinition()
//
//      )
//      .build();
    String schema = """
       type Query {
         testFirst_teacher : [testFirst_teacher]
       }
       type testFirst_teacher {
         id : ID
         name : String
         age : Int
         description : String
         cost : Float
       }
      """;
    SchemaParser schemaParser = new SchemaParser();
    TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);
    DataFetcher<List<Map<String, Object>>> dataFetcher = new DataFetcher<>() {
      @Override
      public List<Map<String, Object>> get(DataFetchingEnvironment environment) throws Exception {
        return session.find(environment.getMergedField().getSingleField().getName(), query -> query);
      }
    };
    RuntimeWiring runtimeWiring = newRuntimeWiring()
      .type("Query", builder ->
        builder.defaultDataFetcher(dataFetcher))
      .build();
    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
    GraphQL graphQL = GraphQL.newGraphQL(graphQLSchema).build();
    ExecutionResult result = graphQL.execute("""
      query {
       testFirst_teacher {
         name,
         id,
         age
       }
      }
      """);
    System.out.println("" + result.getData());
  }

}
