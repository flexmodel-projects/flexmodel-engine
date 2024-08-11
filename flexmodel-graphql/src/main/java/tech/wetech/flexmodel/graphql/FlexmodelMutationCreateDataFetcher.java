package tech.wetech.flexmodel.graphql;

import graphql.schema.DataFetchingEnvironment;
import tech.wetech.flexmodel.SessionFactory;

import java.util.Map;

/**
 * @author cjbi
 */
public class FlexmodelMutationCreateDataFetcher extends FlexmodelAbstractDataFetcher<Map<String, Object>> {

  public FlexmodelMutationCreateDataFetcher(String schemaName, String modelName, SessionFactory sessionFactory) {
    super(schemaName, modelName, sessionFactory);
  }

  @Override
  public Map<String, Object> get(DataFetchingEnvironment environment) throws Exception {
    return Map.of();
  }

}
