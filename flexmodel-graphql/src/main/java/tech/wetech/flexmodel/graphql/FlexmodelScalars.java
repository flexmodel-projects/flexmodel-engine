package tech.wetech.flexmodel.graphql;

import graphql.scalar.GraphqlStringCoercing;
import graphql.schema.GraphQLScalarType;

/**
 * @author cjbi
 */
public class FlexmodelScalars {

  public static final GraphQLScalarType Text = GraphQLScalarType.newScalar()
    .name("Text").description("Built-in String").coercing(new GraphqlStringCoercing()).build();

}
