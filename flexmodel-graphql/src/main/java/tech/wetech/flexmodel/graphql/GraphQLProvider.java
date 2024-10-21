package tech.wetech.flexmodel.graphql;

import graphql.GraphQL;
import graphql.scalars.ExtendedScalars;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import tech.wetech.flexmodel.Entity;
import tech.wetech.flexmodel.Model;
import tech.wetech.flexmodel.SessionFactory;
import tech.wetech.flexmodel.codegen.GenerationTool;
import tech.wetech.flexmodel.codegen.ModelListClass;
import tech.wetech.flexmodel.codegen.ModelListGenerationContext;

import java.util.HashMap;
import java.util.List;
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
    GraphQLSchemaGenerator generator = new GraphQLSchemaGenerator();
    ModelListGenerationContext context = new ModelListGenerationContext();
    ModelListClass modelListClass = new ModelListClass();
    context.setModelListClass(modelListClass);
    Map<String, DataFetcher<?>> queryDataFetchers = new HashMap<>();
    Map<String, DataFetcher<?>> mutationDataFetchers = new HashMap<>();
    for (String schemaName : sf.getSchemaNames()) {
      List<Model> models = sf.getModels(schemaName);
      for (Model model : models) {
        if (model instanceof Entity entity) {
          modelListClass.getModelList().add(GenerationTool.buildModelClass("", schemaName, entity));
        } else {
          // todo 支持非实体类型
        }
      }
    }

    for (String schemaName : sf.getSchemaNames()) {
      List<Model> models = sf.getModels(schemaName);
      for (Model model : models) {
        if (model instanceof Entity entity) {
          for (DataFetchers fetchType : DataFetchers.values()) {
            if(fetchType.isQuery()) {
              queryDataFetchers.put(
                fetchType.getKeyFunc().apply(schemaName, model.getName()),
                fetchType.getDataFetcherFunc().apply(schemaName, model.getName(), sf));
            }
            if(fetchType.isMutation()) {
              mutationDataFetchers.put(
                fetchType.getKeyFunc().apply(schemaName, model.getName()),
                fetchType.getDataFetcherFunc().apply(schemaName, model.getName(), sf));
            }
          }
        }
      }
    }

    String schemaString = generator.generate(context);

    SchemaParser schemaParser = new SchemaParser();
    TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schemaString);

    // 创建 CodeRegistry
    GraphQLCodeRegistry codeRegistry = GraphQLCodeRegistry
      .newCodeRegistry()
      .dataFetchers("Query", queryDataFetchers)
      .dataFetchers("Mutation", mutationDataFetchers)
      .build();

    RuntimeWiring runtimeWiring = newRuntimeWiring()
      .codeRegistry(codeRegistry)
      .scalar(ExtendedScalars.GraphQLLong)
      .scalar(ExtendedScalars.Json)
      .scalar(ExtendedScalars.DateTime)
      .scalar(ExtendedScalars.Date)
      .build();
    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
    this.graphQL = GraphQL.newGraphQL(graphQLSchema).build();
  }

  public GraphQL getGraphQL() {
    return graphQL;
  }

}
