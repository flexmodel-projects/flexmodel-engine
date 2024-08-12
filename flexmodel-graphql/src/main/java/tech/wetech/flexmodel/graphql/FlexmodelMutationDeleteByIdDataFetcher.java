package tech.wetech.flexmodel.graphql;

import graphql.schema.DataFetchingEnvironment;
import tech.wetech.flexmodel.Session;
import tech.wetech.flexmodel.SessionFactory;

import java.util.Map;

/**
 * @author cjbi
 */
public class FlexmodelMutationDeleteByIdDataFetcher extends FlexmodelAbstractDataFetcher<Map<String, Object>> {

  public FlexmodelMutationDeleteByIdDataFetcher(String schemaName, String modelName, SessionFactory sessionFactory) {
    super(schemaName, modelName, sessionFactory);
  }

  @Override
  public Map<String, Object> get(DataFetchingEnvironment environment) throws Exception {
    Object id = environment.getArgument("id");
    try (Session session = sessionFactory.createSession(schemaName)) {
      Map<String, Object> data = session.findById(modelName, id);
      session.deleteById(modelName, id);
      return data;
    }
  }

}
