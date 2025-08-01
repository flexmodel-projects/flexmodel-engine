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
import tech.wetech.flexmodel.codegen.EnumClass;
import tech.wetech.flexmodel.codegen.GenerationContext;
import tech.wetech.flexmodel.codegen.ModelClass;
import tech.wetech.flexmodel.core.SchemaObject;
import tech.wetech.flexmodel.core.model.EntityDefinition;
import tech.wetech.flexmodel.core.model.EnumDefinition;
import tech.wetech.flexmodel.core.session.SessionFactory;

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
    GenerationContext context = new GenerationContext();
    Map<String, DataFetcher<?>> queryDataFetchers = new HashMap<>();
    Map<String, DataFetcher<?>> mutationDataFetchers = new HashMap<>();

    Map<String, Map<String, DataFetcher<?>>> joinDataFetchers = new HashMap<>();
    Map<String, DataFetcher<?>> joinMap = Map.of(
      "_join", (DataFetcher<?>) environment -> Map.of(),
      "_join_mutation", (DataFetcher<?>) environment -> Map.of()
    );
    joinDataFetchers.put("mutation_response", joinMap);

    for (String schemaName : sf.getSchemaNames()) {
      List<SchemaObject> models = sf.getModels(schemaName);
      for (SchemaObject model : models) {
        if (model instanceof EntityDefinition entity) {
          context.getModelClassList().add(ModelClass.buildModelClass("", schemaName, entity));
          joinDataFetchers.put(schemaName + "_" + model.getName(), joinMap);
          joinDataFetchers.put(schemaName + "_" + model.getName() + "_aggregate", joinMap);
        } else if (model instanceof EnumDefinition andEnum) {
          context.getEnumClassList().add(EnumClass.buildEnumClass("", schemaName, andEnum));
        } else {
          // todo 支持非实体类型
        }
      }
    }

    for (String schemaName : sf.getSchemaNames()) {
      List<SchemaObject> models = sf.getModels(schemaName);
      for (SchemaObject model : models) {
        if (model instanceof EntityDefinition entity) {
          for (DataFetchers fetchType : DataFetchers.values()) {
            if (fetchType.isQuery()) {
              queryDataFetchers.put(
                fetchType.getKeyFunc().apply(schemaName, model.getName()),
                fetchType.getDataFetcherFunc().apply(schemaName, model.getName(), sf));
            }
            if (fetchType.isMutation()) {
              mutationDataFetchers.put(
                fetchType.getKeyFunc().apply(schemaName, model.getName()),
                fetchType.getDataFetcherFunc().apply(schemaName, model.getName(), sf));
            }
          }
        }
      }
    }

    String schemaString = generator.generate(context).getFirst();

    SchemaParser schemaParser = new SchemaParser();
    TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schemaString);

    // 创建 CodeRegistry
    GraphQLCodeRegistry codeRegistry = GraphQLCodeRegistry
      .newCodeRegistry()
      .dataFetchers("Query", queryDataFetchers)
      .dataFetchers("Mutation", mutationDataFetchers)
      .build();

    // supports _join/_join_mutation
    codeRegistry = codeRegistry.transform(builder -> joinDataFetchers.forEach(builder::dataFetchers));

    RuntimeWiring runtimeWiring = newRuntimeWiring()
      .codeRegistry(codeRegistry)
      .scalar(ExtendedScalars.Json)
      .scalar(ExtendedScalars.DateTime)
      .scalar(ExtendedScalars.Date)
      .scalar(ExtendedScalars.Time)
      .build();
    SchemaGenerator schemaGenerator = new SchemaGenerator();
    GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
    this.graphQL = GraphQL.newGraphQL(graphQLSchema)
      .instrumentation(new FlexmodelInstrumentation())
      .build();
  }

  public GraphQL getGraphQL() {
    return graphQL;
  }

}
