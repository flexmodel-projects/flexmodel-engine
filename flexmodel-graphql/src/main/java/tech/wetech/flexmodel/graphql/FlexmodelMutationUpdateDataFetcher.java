package tech.wetech.flexmodel.graphql;

import graphql.schema.DataFetchingEnvironment;
import tech.wetech.flexmodel.Session;
import tech.wetech.flexmodel.SessionFactory;

import java.util.Map;

/**
 * @author cjbi
 */
public class FlexmodelMutationUpdateDataFetcher extends FlexmodelAbstractDataFetcher<Map<String, Object>> {

  public FlexmodelMutationUpdateDataFetcher(String schemaName, String modelName, SessionFactory sessionFactory) {
    super(schemaName, modelName, sessionFactory);
  }

  @Override
  public Map<String, Object> get(DataFetchingEnvironment environment) throws Exception {
    Map<String, Object> where = getArgument(environment, WHERE);
    Map<String, Object> setValue = getArgument(environment, "_set");
    final String filter = where != null ? jsonObjectConverter.toJsonString(where) : null;
    assert setValue != null;
    try (Session session = sessionFactory.createSession(schemaName)) {
      int rows = session.update(modelName, setValue, filter);
      return Map.of(AFFECTED_ROWS, rows);
    }
  }

}
