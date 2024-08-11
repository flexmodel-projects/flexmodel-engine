package tech.wetech.flexmodel.graphql;

import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import tech.wetech.flexmodel.SessionFactory;

import java.util.HashMap;
import java.util.Map;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

/**
 * @author cjbi
 */
public class GraphQLProvider {
  private final SessionFactory sf;
  private GraphQL graphQL;

  public GraphQLProvider(SessionFactory sessionFactory) {
    this.sf = sessionFactory;
  }

  public void init() {
    GraphQLSchemaProcessor processor = new GraphQLSchemaProcessor(sf);
    processor.execute();
    Map<String, QueryRootInfo> dataFetcherTypes = processor.getDataFetcherTypes();
    Map<String, DataFetcher<?>> dataFetchers = new HashMap<>();
    dataFetcherTypes.forEach((key, value) -> dataFetchers.put(key, new FlexmodelDataFetcher(value.getSchemaName(), sf)));
    String schemaString = processor.getGraphqlSchemaString();
    System.out.println(schemaString);
    SchemaParser schemaParser = new SchemaParser();

    TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schemaString);

    // 创建 CodeRegistry
    GraphQLCodeRegistry codeRegistry = GraphQLCodeRegistry
      .newCodeRegistry()
      .dataFetchers("Query", dataFetchers)
      .build();

    RuntimeWiring runtimeWiring = newRuntimeWiring()
      .codeRegistry(codeRegistry)
      .build();
    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
    this.graphQL = GraphQL.newGraphQL(graphQLSchema).build();
  }

  public GraphQL getGraphQL() {
    return graphQL;
  }

}
