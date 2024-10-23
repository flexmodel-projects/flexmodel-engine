package tech.wetech.flexmodel.graphql;

import graphql.schema.DataFetchingEnvironment;
import tech.wetech.flexmodel.Session;
import tech.wetech.flexmodel.SessionFactory;

import java.util.Map;

/**
 * @author cjbi
 */
public class FlexmodelMutationUpdateByIdDataFetcher extends FlexmodelAbstractDataFetcher<Map<String, Object>> {

  public FlexmodelMutationUpdateByIdDataFetcher(String schemaName, String modelName, SessionFactory sessionFactory) {
    super(schemaName, modelName, sessionFactory);
  }

  @Override
  public Map<String, Object> get(DataFetchingEnvironment environment) throws Exception {
    Object id = getArgument(environment, ID);
    Map<String, Object> setValue = getArgument(environment, "_set");
    assert setValue != null;
    try (Session session = sessionFactory.createSession(schemaName)) {
      Map<String, Object> data = session.findById(modelName, id);
      data.putAll(setValue);
      session.updateById(modelName, data, id);
      return data;
    }
  }

}
