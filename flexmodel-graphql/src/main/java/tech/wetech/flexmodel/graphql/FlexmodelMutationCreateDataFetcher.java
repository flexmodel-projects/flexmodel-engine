package tech.wetech.flexmodel.graphql;

import graphql.schema.DataFetchingEnvironment;
import tech.wetech.flexmodel.EntityDefinition;
import tech.wetech.flexmodel.Session;
import tech.wetech.flexmodel.SessionFactory;
import tech.wetech.flexmodel.TypedField;

import java.util.Map;
import java.util.Optional;

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
        EntityDefinition entity = (EntityDefinition) session.getModel(modelName);
        session.insert(modelName, data);
        Optional<TypedField<?, ?>> idFieldOptional = entity.findIdField();
        return data;
      }
    }
    return null;
  }

}
