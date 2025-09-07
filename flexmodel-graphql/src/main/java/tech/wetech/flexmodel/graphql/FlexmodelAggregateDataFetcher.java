package tech.wetech.flexmodel.graphql;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;
import tech.wetech.flexmodel.model.EntityDefinition;
import tech.wetech.flexmodel.model.field.TypedField;
import tech.wetech.flexmodel.query.Direction;
import tech.wetech.flexmodel.query.Query;
import tech.wetech.flexmodel.session.Session;
import tech.wetech.flexmodel.session.SessionFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tech.wetech.flexmodel.query.Query.*;

/**
 * @author cjbi
 */
public class FlexmodelAggregateDataFetcher extends FlexmodelAbstractDataFetcher<Map<String, Object>> {

  public FlexmodelAggregateDataFetcher(String schemaName, String modelName, SessionFactory sf) {
    super(schemaName, modelName, sf);
  }

  @Override
  public Map<String, Object> get(DataFetchingEnvironment env) {
    return findRootData(env);
  }

  public Map<String, Object> findRootData(DataFetchingEnvironment env) {
    Integer pageNumber = getArgument(env, PAGE_NUMBER);
    Integer pageSize = getArgument(env, PAGE_SIZE);
    Map<String, String> orderBy = getArgument(env, ORDER_BY);
    Map<String, Object> where = getArgument(env, WHERE);
    List<SelectedField> selectedFields = env.getSelectionSet().getImmediateFields();
    try (Session session = sessionFactory.createSession(schemaName)) {
      EntityDefinition entity = (EntityDefinition) session.schema().getModel(modelName);

      String whereString = null;
      if (where != null) {
        whereString = jsonObjectConverter.toJsonString(where);
      }

      OrderBy sort;
      if (orderBy != null) {
        sort = new OrderBy();
        orderBy.forEach((k, v) -> sort.addOrder(k, Direction.valueOf(v.toUpperCase())));
      } else {
        sort = null;
      }

      Query.Page page = null;
      if (pageSize != null && pageNumber != null) {
        page = new Query.Page().setPageNumber(pageNumber).setPageSize(pageSize);
      }

      List<Map<String, Object>> list = session.dsl().select(projection -> {
          for (SelectedField selectedField : selectedFields) {
            if (selectedField.getName().equals(AGG_COUNT)) {
              Map<String, Object> args = selectedField.getArguments();
              Boolean distinct = (Boolean) args.get("distinct");
              String field = (String) args.get("field");
              if (field == null) {
                field = entity.getName() + "." + entity.findIdField().orElseThrow().getName();
              }
              projection.field(selectedField.getName(), count(field(field)));
              continue;
            }
            if (selectedField.getName().equals(AGG_MAX)) {
              List<SelectedField> aggFields = selectedField.getSelectionSet().getImmediateFields();
              for (SelectedField aggField : aggFields) {
                projection.field(AGG_MAX + "_" + aggField.getName(), max(field(aggField.getName())));
              }
              continue;
            }
            if (selectedField.getName().equals(AGG_MIN)) {
              List<SelectedField> aggFields = selectedField.getSelectionSet().getImmediateFields();
              for (SelectedField aggField : aggFields) {
                projection.field(AGG_MIN + "_" + aggField.getName(), min(field(aggField.getName())));
              }
              continue;
            }
            if (selectedField.getName().equals(AGG_SUM)) {
              List<SelectedField> aggFields = selectedField.getSelectionSet().getImmediateFields();
              for (SelectedField aggField : aggFields) {
                projection.field(AGG_SUM + "_" + aggField.getName(), sum(field(aggField.getName())));
              }
              continue;
            }
            if (selectedField.getName().equals(AGG_AVG)) {
              List<SelectedField> aggFields = selectedField.getSelectionSet().getImmediateFields();
              for (SelectedField aggField : aggFields) {
                projection.field(AGG_AVG + "_" + aggField.getName(), avg(field(aggField.getName())));
              }
              continue;
            }
            TypedField<?, ?> flexModelField = entity.getField(selectedField.getName());
            if (flexModelField == null) {
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

      // 不支持关联字段查询
      return toResult(list.get(0));
    }
  }

  private Map<String, Object> toResult(Map<String, Object> map) {
    Map<String, Object> result = new HashMap<>();
    map.forEach((k, v) -> {
      if (k.equals(AGG_COUNT)) {
        result.put(AGG_COUNT, v);
      } else {
        for (String aggField : AGG_FIELDS) {
          if (k.startsWith(aggField)) {
            result.compute(aggField, (k2, v2) -> {
              Map<String, Object> aggMap = (Map<String, Object>) v2;
              if (aggMap == null) {
                aggMap = new HashMap<>();
              }
              aggMap.put(k.substring(aggField.length() + 1), v);
              return aggMap;
            });
          }
        }

      }
    });
    return result;
  }

}
