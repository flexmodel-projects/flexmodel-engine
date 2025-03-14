package tech.wetech.flexmodel.graphql;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;
import tech.wetech.flexmodel.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tech.wetech.flexmodel.Projections.field;

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
      Entity entity = (Entity) session.getModel(modelName);
      IDField idField = entity.findIdField().orElseThrow();

      List<RelationField> relationFields = new ArrayList<>();
      List<Map<String, Object>> list = session.find(entity.getName(), query -> query
        .setFilter(filter)
        .withProjection(projection -> {
          projection.addField(idField.getName(), field(entity.getName() + "." + idField.getName()));
          for (SelectedField selectedField : selectedFields) {
            TypedField<?, ?> flexModelField = entity.getField(selectedField.getName());
            if (flexModelField == null) {
              continue;
            }
            if (flexModelField instanceof RelationField secondaryRelationField) {
              relationFields.add(secondaryRelationField);
              continue;
            }
            projection.addField(selectedField.getName(), field(flexModelField.getModelName() + "." + flexModelField.getName()));
          }
          return projection;
        })
        .withPage(1, 1)
      );
      if (list.isEmpty()) {
        return null;
      }
      Map<String, Object> resultData = new HashMap<>(list.stream().findFirst().orElseThrow());
      for (RelationField sencondaryRelationField : relationFields) {
        Object secondaryId = resultData.get(entity.findIdField().map(IDField::getName).orElseThrow());
        List<Map<String, Object>> relationDataList = findRelationDataList(session, env, null, sencondaryRelationField.getFrom(), sencondaryRelationField, secondaryId);
        resultData.put(sencondaryRelationField.getName(), sencondaryRelationField.isMultiple() ? relationDataList : relationDataList.stream().findFirst().orElse(null));
      }
      return resultData;
    }

  }

}
