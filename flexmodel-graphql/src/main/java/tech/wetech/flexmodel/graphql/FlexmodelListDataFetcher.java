package tech.wetech.flexmodel.graphql;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;
import tech.wetech.flexmodel.model.EntityDefinition;
import tech.wetech.flexmodel.model.field.RelationField;
import tech.wetech.flexmodel.model.field.TypedField;
import tech.wetech.flexmodel.query.Direction;
import tech.wetech.flexmodel.query.Query;
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
      EntityDefinition entity = (EntityDefinition) session.schema().getModel(modelName);
      List<RelationField> relationFields = new ArrayList<>();

      String whereString = null;
      if (where != null) {
        whereString = jsonObjectConverter.toJsonString(where);
      }

      Query.Sort sort;
      if (orderBy != null) {
        sort = new Query.Sort();
        orderBy.forEach((k, v) -> sort.addOrder(k, Direction.valueOf(v.toUpperCase())));
      } else {
        sort = null;
      }

      Query.Page page = null;
      if (pageSize != null && pageNumber != null) {
        page = new Query.Page().setPageNumber(pageNumber).setPageSize(pageSize);
      }

      List<Map<String, Object>> list = session.dsl()
        .select(projection -> {
          TypedField<?, ?> idField = entity.findIdField().orElseThrow();
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
        .where(whereString)
        .orderBy(sort)
        .page(page)
        .execute();

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
