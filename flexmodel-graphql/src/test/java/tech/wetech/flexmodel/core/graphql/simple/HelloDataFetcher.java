package tech.wetech.flexmodel.core.graphql.simple;

/**
 * @author cjbi
 */
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class HelloDataFetcher  implements DataFetcher<String> {
  @Override
  public String get(DataFetchingEnvironment environment) {
    return "Hello, World!";
  }
}
