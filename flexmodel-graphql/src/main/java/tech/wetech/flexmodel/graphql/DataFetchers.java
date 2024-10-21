package tech.wetech.flexmodel.graphql;

import graphql.schema.DataFetcher;
import tech.wetech.flexmodel.SessionFactory;

import java.util.function.BiFunction;

/**
 * @author cjbi
 */
public enum DataFetchers {
  FIND((schema, model) -> schema + "_list" + "_" + model, FlexmodelFindDataFetcher::new, true),
  FIND_ONE((schema, model) -> schema + "_find_one" + "_" + model, FlexmodelFindOneDataFetcher::new, true),
  AGGREGATE((schema, model) -> schema + "_aggregate" + "_" + model, FlexmodelAggregateDataFetcher::new, true),
  MUTATION_DELETE((schema, model) -> schema + "_" + "delete_" + model, FlexmodelMutationDeleteDataFetcher::new, false),
  MUTATION_DELETE_BY_ID((schema, model) -> schema + "_" + "delete_" + model + "_by_id", FlexmodelMutationDeleteByIdDataFetcher::new, false),
  MUTATION_CREATE((schema, model) -> schema + "_" + "create_" + model, FlexmodelMutationCreateDataFetcher::new, false),
  MUTATION_UPDATE((schema, model) -> schema + "_" + "update_" + model, FlexmodelMutationUpdateDataFetcher::new, false),
  MUTATION_UPDATE_BY_ID((schema, model) -> schema + "_" + "update_" + model + "_by_id", FlexmodelMutationUpdateByIdDataFetcher::new, false);

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
