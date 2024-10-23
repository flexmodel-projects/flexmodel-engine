package tech.wetech.flexmodel.graphql;

import graphql.schema.DataFetchingEnvironment;
import tech.wetech.flexmodel.Session;
import tech.wetech.flexmodel.SessionFactory;

import java.util.Map;

/**
 * @author cjbi
 */
public class FlexmodelMutationDeleteDataFetcher extends FlexmodelAbstractDataFetcher<Map<String, Object>> {

  public FlexmodelMutationDeleteDataFetcher(String schemaName, String modelName, SessionFactory sessionFactory) {
    super(schemaName, modelName, sessionFactory);
  }

  @Override
  public Map<String, Object> get(DataFetchingEnvironment environment) throws Exception {
    Map<String, Object> where = getArgument(environment, WHERE);
    String filter = getFilterString(where);
    try (Session session = sessionFactory.createSession(schemaName)) {
      int rows = session.delete(modelName, filter);
      return Map.of(AFFECTED_ROWS, rows);
    }
  }

}
