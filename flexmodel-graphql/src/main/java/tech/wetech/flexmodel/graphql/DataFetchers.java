package tech.wetech.flexmodel.graphql;

import graphql.schema.DataFetcher;
import tech.wetech.flexmodel.SessionFactory;

import java.util.function.BiFunction;

import static tech.wetech.flexmodel.codegen.StringUtils.snakeToCamel;

/**
 * @author cjbi
 */
public enum DataFetchers {
  FIND((schema, model) -> snakeToCamel(schema + "_" + model + "_list"), FlexmodelFindDataFetcher::new, true),
  FIND_ONE((schema, model) -> snakeToCamel(schema + "_" + model), FlexmodelFindOneDataFetcher::new, true),
  AGGREGATE((schema, model) -> snakeToCamel(schema + "_" + model + "_aggregate"), FlexmodelAggregateDataFetcher::new, true),
  MUTATION_DELETE((schema, model) -> snakeToCamel("delete_" + schema + "_" + model), FlexmodelMutationDeleteDataFetcher::new, false),
  MUTATION_DELETE_BY_ID((schema, model) -> snakeToCamel("delete_" + schema + "_" + model + "_by_id"), FlexmodelMutationDeleteByIdDataFetcher::new, false),
  MUTATION_CREATE((schema, model) -> snakeToCamel("create_" + schema + "_" + model), FlexmodelMutationCreateDataFetcher::new, false),
  MUTATION_UPDATE((schema, model) -> snakeToCamel("update_" + schema + "_" + model), FlexmodelMutationUpdateDataFetcher::new, false),
  MUTATION_UPDATE_BY_ID((schema, model) -> snakeToCamel("update_" + schema + "_" + model + "_by_id"), FlexmodelMutationUpdateByIdDataFetcher::new, false);

  private final BiFunction<String, String, String> keyFunc;
  private final DataFetcherFunc dataFetcherFunc;
  private final boolean query;

  DataFetchers(BiFunction<String, String, String> keyFunc, DataFetcherFunc dataFetcherFunc, boolean query) {
    this.keyFunc = keyFunc;
    this.dataFetcherFunc = dataFetcherFunc;
    this.query = query;
  }

  public BiFunction<String, String, String> getKeyFunc() {
    return keyFunc;
  }

  public DataFetcherFunc getDataFetcherFunc() {
    return dataFetcherFunc;
  }

  public boolean isQuery() {
    return query;
  }

  public boolean isMutation() {
    return !query;
  }

  @FunctionalInterface
  public interface DataFetcherFunc {
    DataFetcher<?> apply(String t, String u, SessionFactory sf);
  }
}
