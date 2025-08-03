package tech.wetech.flexmodel.graphql;

import graphql.schema.DataFetchingEnvironment;
import tech.wetech.flexmodel.session.Session;
import tech.wetech.flexmodel.session.SessionFactory;

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

      Map<String, Object> data = session.dsl()
        .select()
        .from(modelName)
        .where(filter)
        .executeOne();

      data.putAll(setValue);

      int rows = session.dsl()
        .update(modelName)
        .values(data)
        .where(filter)
        .execute();

      return Map.of(AFFECTED_ROWS, rows);
    }
  }

}
