package tech.wetech.flexmodel.graphql;

import graphql.schema.DataFetchingEnvironment;
import tech.wetech.flexmodel.JsonUtils;
import tech.wetech.flexmodel.session.Session;
import tech.wetech.flexmodel.session.SessionFactory;

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
    final String filter = where != null ? JsonUtils.toJsonString(where) : null;
    try (Session session = sessionFactory.createSession(schemaName)) {

      int rows = session.dsl()
        .deleteFrom(modelName)
        .where(filter)
        .execute();

      return Map.of(AFFECTED_ROWS, rows);
    }
  }

}
