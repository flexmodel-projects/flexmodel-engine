package tech.wetech.flexmodel.graphql;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;
import tech.wetech.flexmodel.core.model.EntityDefinition;
import tech.wetech.flexmodel.core.model.field.RelationField;
import tech.wetech.flexmodel.core.model.field.TypedField;
import tech.wetech.flexmodel.core.session.Session;
import tech.wetech.flexmodel.core.session.SessionFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tech.wetech.flexmodel.core.query.Projections.field;

/**
 * @author cjbi
 */
public class FlexmodelListDataFetcher extends FlexmodelAbstractDataFetcher<List<Map<String, Object>>> {

  public FlexmodelListDataFetcher(String schemaName, String modelName, SessionFactory sessionFactory) {
    super(schemaName, modelName, sessionFactory);
  }

  @Override
  public List<Map<String, Object>> get(DataFetchingEnvironment env) {
    return findRootData(env);
  }

  public List<Map<String, Object>> findRootData(DataFetchingEnvironment env) {
    Integer pageNumber = getArgument(env, PAGE_NUMBER);
    Integer pageSize = getArgument(env, PAGE_SIZE);
    Map<String, String> orderBy = getArgument(env, ORDER_BY);
    Map<String, Object> where = getArgument(env, WHERE);
    List<SelectedField> selectedFields = env.getSelectionSet().getImmediateFields();
    try (Session session = sessionFactory.createSession(schemaName)) {
      EntityDefinition entity = (EntityDefinition) session.getModel(modelName);
      List<RelationField> relationFields = new ArrayList<>();
      List<Map<String, Object>> list = session.find(entity.getName(), query -> {
        query.select(projection -> {
            TypedField<?, ?> idField = entity.findIdField().orElseThrow();
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
          return getQuery(pageNumber, pageSize, orderBy, where, query);
        }
      );
      List<Map<String, Object>> result = new ArrayList<>();
      for (Map<String, Object> map : list) {
        Map<String, Object> resultData = new HashMap<>(map);
        result.add(resultData);
        for (RelationField sencondaryRelationField : relationFields) {
          Object secondaryId = map.get(entity.findIdField().map(TypedField::getName).orElseThrow());
          List<Map<String, Object>> relationDataList = findRelationDataList(session, env, null, sencondaryRelationField.getFrom(), sencondaryRelationField, secondaryId);
          resultData.put(sencondaryRelationField.getName(),
            sencondaryRelationField.isMultiple() ? relationDataList : relationDataList.stream().findFirst().orElse(null));
        }
      }
      return result;
    }
  }


}
