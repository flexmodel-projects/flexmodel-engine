package tech.wetech.flexmodel.core.graphql.simple;


import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.Scalars;
import graphql.schema.*;

/**
 * @author cjbi
 */
public class GraphQLSetup {
  public static void main(String[] args) {
    // 创建 DataFetcher
    HelloDataFetcher helloDataFetcher = new HelloDataFetcher();

    // 创建 CodeRegistry
    GraphQLCodeRegistry codeRegistry = GraphQLCodeRegistry
      .newCodeRegistry()
      .dataFetcher(FieldCoordinates.coordinates("Query", "hello"), helloDataFetcher) // 注册数据获取器
      .build();

    // 创建 GraphQL schema
    GraphQLSchema schema = GraphQLSchema.newSchema()
      .query(GraphQLObjectType.newObject()
        .name("Query")
        .field(GraphQLFieldDefinition.newFieldDefinition()
          .name("hello")
          .type(Scalars.GraphQLString))
        .build())
      .codeRegistry(codeRegistry)
      .build();

    // 执行查询
    GraphQL graphQL = GraphQL.newGraphQL(schema).build();

    // 创建查询
    String query = "query { hello }";
    ExecutionResult executionResult = graphQL.execute(query);

    // 打印结果
    System.out.println(executionResult.getData().toString());
  }
}
