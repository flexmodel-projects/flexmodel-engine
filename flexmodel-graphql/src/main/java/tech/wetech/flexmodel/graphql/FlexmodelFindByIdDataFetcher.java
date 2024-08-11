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
public class FlexmodelFindByIdDataFetcher extends FlexmodelAbstractDataFetcher<Map<String, Object>> {

  private final String schemaName;
  private final String modelName;
  private final SessionFactory sessionFactory;

  public FlexmodelFindByIdDataFetcher(String schemaName, String modelName, SessionFactory sessionFactory) {
    this.schemaName = schemaName;
    this.modelName = modelName;
    this.sessionFactory = sessionFactory;
  }

  @Override
  public Map<String, Object> get(DataFetchingEnvironment env) {
    return findRootData(env);
  }

  public Map<String, Object> findRootData(DataFetchingEnvironment env) {
    List<SelectedField> selectedFields = env.getSelectionSet().getImmediateFields();
    try (Session session = sessionFactory.createSession(schemaName)) {
      Entity entity = (Entity) session.getModel(modelName);
      IDField idField = entity.findIdField().orElseThrow();
      Object idValue = env.getArgument(idField.getName());
      List<RelationField> relationFields = new ArrayList<>();
      List<Map<String, Object>> list = session.find(entity.getName(), query -> query
        .setFilter(String.format("""
          {
             "==": [{ "var": ["%s"] }, %s]
          }
          """, idField.getName(), idValue instanceof String ? "\"" + idValue + "\"" : idValue))
        .setProjection(projection -> {
          projection.addField(idField.getName(), field(entity.getName() + "." + idField.getName()));
          for (SelectedField selectedField : selectedFields) {
            TypedField<?, ?> flexModelField = (TypedField<?, ?>) entity.getField(selectedField.getName());
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
      );
      if (list.isEmpty()) {
        return null;
      }
      Map<String, Object> resultData = new HashMap<>(list.stream().findFirst().orElseThrow());
      for (RelationField sencondaryRelationField : relationFields) {
        Object secondaryId = resultData.get(entity.findIdField().map(IDField::getName).orElseThrow());
        resultData.put(sencondaryRelationField.getName(),
          sencondaryRelationField.getCardinality() == RelationField.Cardinality.ONE_TO_ONE ?
            findAssociationDataList(session, env, null, sencondaryRelationField.getTargetEntity(), sencondaryRelationField, secondaryId).stream()
              .findFirst()
              .orElse(null)
            : findAssociationDataList(session, env, null, sencondaryRelationField.getTargetEntity(), sencondaryRelationField, secondaryId));
      }
      return resultData;
    }

  }

}
