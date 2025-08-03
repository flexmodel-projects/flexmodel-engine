package tech.wetech.flexmodel.graphql;

import graphql.schema.DataFetchingEnvironment;
import tech.wetech.flexmodel.model.EntityDefinition;
import tech.wetech.flexmodel.model.field.TypedField;
import tech.wetech.flexmodel.query.expr.Expressions;
import tech.wetech.flexmodel.session.Session;
import tech.wetech.flexmodel.session.SessionFactory;

import java.util.Map;
import java.util.Optional;

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

      EntityDefinition entity = (EntityDefinition) session.getModel(modelName);
      Optional<TypedField<?, ?>> idFieldOptional = entity.findIdField();

      Map<String, Object> data = session.dsl()
        .select()
        .from(modelName)
        .where(Expressions.field(idFieldOptional.get().getName()).eq(id))
        .executeOne();

      data.putAll(setValue);

      session.dsl()
        .update(modelName)
        .values(data)
        .where(Expressions.field(idFieldOptional.get().getName()).eq(id))
        .execute();

      return data;
    }
  }

}
