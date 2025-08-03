package tech.wetech.flexmodel.graphql;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;
import tech.wetech.flexmodel.model.EntityDefinition;
import tech.wetech.flexmodel.model.field.RelationField;
import tech.wetech.flexmodel.model.field.TypedField;
import tech.wetech.flexmodel.session.Session;
import tech.wetech.flexmodel.session.SessionFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tech.wetech.flexmodel.query.Query.field;


/**
 * @author cjbi
 */
public class FlexmodelFindOneDataFetcher extends FlexmodelAbstractDataFetcher<Map<String, Object>> {

  public FlexmodelFindOneDataFetcher(String schemaName, String modelName, SessionFactory sessionFactory) {
    super(schemaName, modelName, sessionFactory);
  }

  @Override
  public Map<String, Object> get(DataFetchingEnvironment env) {
    return findRootData(env);
  }

  private Map<String, Object> findRootData(DataFetchingEnvironment env) {
    List<SelectedField> selectedFields = env.getSelectionSet().getImmediateFields();
    Map<String, Object> where = getArgument(env, WHERE);
    final String filter = where != null ? jsonObjectConverter.toJsonString(where) : null;
    try (Session session = sessionFactory.createSession(schemaName)) {
      EntityDefinition entity = (EntityDefinition) session.schema().getModel(modelName);
      TypedField<?, ?> idField = entity.findIdField().orElseThrow();

      List<RelationField> relationFields = new ArrayList<>();

      List<Map<String, Object>> list = session.dsl()
        .select(projection -> {
          projection.field(idField.getName(), field(entity.getName() + "." + idField.getName()));
          for (SelectedField selectedField : selectedFields) {
            TypedField<?, ?> flexModelField = entity.getField(selectedField.getName());
            if (flexModelField == null) {
              continue;
            }
            if (flexModelField instanceof RelationField secondaryRelationField) {
              relationFields.add(secondaryRelationField);
              continue;
            }
            projection.field(selectedField.getName(), field(flexModelField.getModelName() + "." + flexModelField.getName()));
          }
          return projection;
        })
        .from(entity.getName())
        .where(filter)
        .page(1, 1)
        .execute();

      if (list.isEmpty()) {
        return null;
      }
      Map<String, Object> resultData = new HashMap<>(list.stream().findFirst().orElseThrow());
      for (RelationField sencondaryRelationField : relationFields) {
        Object secondaryId = resultData.get(entity.findIdField().map(TypedField::getName).orElseThrow());
        List<Map<String, Object>> relationDataList = findRelationDataList(session, env, null, sencondaryRelationField.getFrom(), sencondaryRelationField, secondaryId);
        resultData.put(sencondaryRelationField.getName(), sencondaryRelationField.isMultiple() ? relationDataList : relationDataList.stream().findFirst().orElse(null));
      }
      return resultData;
    }

  }

}
