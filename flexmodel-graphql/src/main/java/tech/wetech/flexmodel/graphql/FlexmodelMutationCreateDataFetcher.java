package tech.wetech.flexmodel.graphql;

import graphql.schema.DataFetchingEnvironment;
import tech.wetech.flexmodel.core.model.EntityDefinition;
import tech.wetech.flexmodel.core.model.field.TypedField;
import tech.wetech.flexmodel.core.session.Session;
import tech.wetech.flexmodel.core.session.SessionFactory;

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
