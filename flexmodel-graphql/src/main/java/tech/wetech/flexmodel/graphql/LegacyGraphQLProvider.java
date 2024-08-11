package tech.wetech.flexmodel.graphql;

import graphql.GraphQL;
import graphql.scalars.ExtendedScalars;
import graphql.schema.*;
import graphql.util.TraversalControl;
import graphql.util.TraverserContext;
import tech.wetech.flexmodel.*;

import java.util.*;

import static graphql.Scalars.*;
import static graphql.schema.GraphQLList.list;
import static graphql.schema.GraphQLObjectType.newObject;
import static graphql.util.TraversalControl.CONTINUE;
import static graphql.util.TraversalControl.QUIT;
import static tech.wetech.flexmodel.RelationField.Cardinality.ONE_TO_ONE;

/**
 * @author cjbi
 */
@Deprecated
public class LegacyGraphQLProvider {
  private final SessionFactory sessionFactory;
  private GraphQL graphQL;
  private final Map<String, GraphQLScalarType> typeMapping = new HashMap<>();

  public LegacyGraphQLProvider(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;

    typeMapping.put(ScalarType.ID.getType(), GraphQLID);
    typeMapping.put(ScalarType.STRING.getType(), GraphQLString);
    typeMapping.put(ScalarType.TEXT.getType(), FlexmodelScalars.Text);
    typeMapping.put(ScalarType.DECIMAL.getType(), GraphQLFloat);
    typeMapping.put(ScalarType.INT.getType(), GraphQLInt);
    typeMapping.put(ScalarType.BIGINT.getType(), ExtendedScalars.GraphQLBigInteger);
    typeMapping.put(ScalarType.BOOLEAN.getType(), GraphQLBoolean);
    typeMapping.put(ScalarType.DATETIME.getType(), ExtendedScalars.DateTime);
    typeMapping.put(ScalarType.DATE.getType(), ExtendedScalars.Date);
    typeMapping.put(ScalarType.JSON.getType(), ExtendedScalars.Json);
  }

  public void init() {
    Set<String> schemaNames = sessionFactory.getSchemaNames();
    Map<String, DataFetcher<?>> dataFetchers = new HashMap<>();
    List<GraphQLObjectType> objectTypes = new ArrayList<>();
    for (String schemaName : schemaNames) {
      List<Model> models = sessionFactory.getModels(schemaName);
      for (Model model : models) {
        if (model instanceof Entity entity && entity.findIdField().isPresent()) {
          List<GraphQLFieldDefinition> fieldDefinitions = new ArrayList<>();
          for (TypedField<?, ?> field : entity.getFields()) {
            if (field instanceof RelationField relationField) {
              GraphQLOutputType relationType;
              if (relationField.getCardinality() == ONE_TO_ONE) {
                relationType = GraphQLTypeReference.typeRef(schemaName + "_" + relationField.getTargetEntity());
              } else {
                relationType = list(GraphQLTypeReference.typeRef(schemaName + "_" + relationField.getTargetEntity()));
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
            .name(schemaName + "_" + entity.getName())
            .fields(fieldDefinitions)
            .description(entity.getComment())
            .build());
          dataFetchers.put(schemaName + "_" + entity.getName(), new FlexmodelDataFetcher(schemaName, sessionFactory));
        }
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
      .name("schema")
      .fields(queryFieldDefinitions)
      .build();

//    for (GraphQLFieldDefinition queryFieldDefinition : queryFieldDefinitions) {
//      dataFetchers.put(queryFieldDefinition.getName(), new FlexModelDataFetcher("system", sessionFactory));
//    }

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

    this.graphQL = GraphQL.newGraphQL(schema).build();
  }

  public void transformDelete(String schemaName, Model model) {
    GraphQLTypeVisitorStub visitor = new GraphQLTypeVisitorStub() {
      @Override
      public TraversalControl visitGraphQLObjectType(GraphQLObjectType node, TraverserContext<GraphQLSchemaElement> context) {
        return execute(node.getName(), schemaName, model, context);
      }

      @Override
      public TraversalControl visitGraphQLFieldDefinition(GraphQLFieldDefinition node, TraverserContext<GraphQLSchemaElement> context) {
        return execute(node.getName(), schemaName, model, context);
      }

      private TraversalControl execute(String node, String schemaName, Model model, TraverserContext<GraphQLSchemaElement> context) {
        if (node.equals(schemaName + "_" + model.getName())) {
          deleteNode(context);
        }
        return CONTINUE;
      }
    };
    GraphQLSchema newGraphQLSchema = SchemaTransformer.transformSchema(graphQL.getGraphQLSchema(), visitor);
    this.graphQL = GraphQL.newGraphQL(newGraphQLSchema).build();
  }

  public void transformInsert(String schemaName, Model model) {
    GraphQLTypeVisitorStub visitor = new GraphQLTypeVisitorStub() {
      @Override
      public TraversalControl visitGraphQLFieldDefinition(GraphQLFieldDefinition node, TraverserContext<GraphQLSchemaElement> context) {
        if (!node.getName().startsWith(schemaName + "_")) {
          return CONTINUE;
        }
        GraphQLObjectType objectType = modelToGraphQLType(schemaName, model);
        GraphQLCodeRegistry.Builder codeRegistry = context.getVarFromParents(GraphQLCodeRegistry.Builder.class);
        FieldCoordinates coordinates = FieldCoordinates.coordinates("Query", objectType.getName());
        codeRegistry.dataFetcher(coordinates, new FlexmodelDataFetcher(schemaName, sessionFactory));

        // 插入元素
        GraphQLSchemaElement insertElement = GraphQLFieldDefinition.newFieldDefinition()
          .name(objectType.getName())
          .type(list(objectType)).build();

        insertAfter(context, insertElement);
        return QUIT;
      }
    };
    GraphQLSchema newGraphQLSchema = SchemaTransformer.transformSchema(graphQL.getGraphQLSchema(), visitor);
    this.graphQL = GraphQL.newGraphQL(newGraphQLSchema).build();
  }

  private GraphQLObjectType modelToGraphQLType(String schemaName, Model model) {
    GraphQLObjectType graphQLObjectType = null;
    if (model instanceof Entity entity && entity.findIdField().isPresent()) {
      List<GraphQLFieldDefinition> fieldDefinitions = new ArrayList<>();
      for (TypedField<?, ?> field : entity.getFields()) {
        if (field instanceof RelationField relationField) {
          GraphQLOutputType relationType;
          if (relationField.getCardinality() == ONE_TO_ONE) {
            relationType = GraphQLTypeReference.typeRef(schemaName + "_" + relationField.getTargetEntity());
          } else {
            relationType = list(GraphQLTypeReference.typeRef(schemaName + "_" + relationField.getTargetEntity()));
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
      graphQLObjectType = newObject()
        .name(schemaName + "_" + entity.getName())
        .fields(fieldDefinitions)
        .description(entity.getComment())
        .build();
    }
    return graphQLObjectType;
  }

  public GraphQL getGraphQL() {
    return graphQL;
  }

}
