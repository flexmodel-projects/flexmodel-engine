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
public class FlexmodelFindDataFetcher extends FlexmodelAbstractDataFetcher<List<Map<String, Object>>> {

  public FlexmodelFindDataFetcher(String schemaName, String modelName, SessionFactory sessionFactory) {
    super(schemaName, modelName, sessionFactory);
  }

  @Override
  public List<Map<String, Object>> get(DataFetchingEnvironment env) {
    return findRootData(env);
  }

  public List<Map<String, Object>> findRootData(DataFetchingEnvironment env) {
    Integer offset = env.getArgument("offset");
    Integer limit = env.getArgument("limit");

    List<SelectedField> selectedFields = env.getSelectionSet().getImmediateFields();
    try (Session session = sessionFactory.createSession(schemaName)) {
      Entity entity = (Entity) session.getModel(modelName);
      List<RelationField> relationFields = new ArrayList<>();
      List<Map<String, Object>> list = session.find(entity.getName(), query -> {
          query.setProjection(projection -> {
            IDField idField = entity.findIdField().orElseThrow();
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
          });
          if (limit != null) {
            query.setLimit(limit);
            if (offset != null) {
              query.setOffset(offset);
            }
          }
          return query;
        }
      );
      List<Map<String, Object>> result = new ArrayList<>();
      for (Map<String, Object> map : list) {
        Map<String, Object> resultData = new HashMap<>(map);
        result.add(resultData);
        for (RelationField sencondaryRelationField : relationFields) {
          Object secondaryId = map.get(entity.findIdField().map(IDField::getName).orElseThrow());
          resultData.put(sencondaryRelationField.getName(),
            sencondaryRelationField.getCardinality() == RelationField.Cardinality.ONE_TO_ONE ?
              findAssociationDataList(session, env, null, sencondaryRelationField.getTargetEntity(), sencondaryRelationField, secondaryId).stream()
                .findFirst()
                .orElse(null)
              : findAssociationDataList(session, env, null, sencondaryRelationField.getTargetEntity(), sencondaryRelationField, secondaryId));
        }
      }
      return result;
    }
  }

}
