package tech.wetech.flexmodel.graphql;

import graphql.schema.DataFetchingEnvironment;
import tech.wetech.flexmodel.session.Session;
import tech.wetech.flexmodel.session.SessionFactory;

import java.util.Map;

/**
 * @author cjbi
 */
public class FlexmodelMutationCreateDataFetcher extends FlexmodelAbstractDataFetcher<Map<String, Object>> {

  public FlexmodelMutationCreateDataFetcher(String schemaName, String modelName, SessionFactory sessionFactory) {
    super(schemaName, modelName, sessionFactory);
  }

  @Override
  @SuppressWarnings("all")
  public Map<String, Object> get(DataFetchingEnvironment environment) throws Exception {
    Map<String, Object> arguments = getArguments(environment);
    if (arguments.get("data") instanceof Map data) {
      try (Session session = sessionFactory.createSession(schemaName)) {

        session.dsl()
          .insertInto(modelName)
          .values(data)
          .execute();

        return data;
      }
    }
    return null;
  }

}
