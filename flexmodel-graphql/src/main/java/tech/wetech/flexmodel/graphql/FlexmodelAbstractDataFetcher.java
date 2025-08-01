package tech.wetech.flexmodel.graphql;

import graphql.execution.CoercedVariables;
import graphql.execution.ValuesResolver;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;
import tech.wetech.flexmodel.JsonObjectConverter;
import tech.wetech.flexmodel.model.EntityDefinition;
import tech.wetech.flexmodel.model.field.RelationField;
import tech.wetech.flexmodel.model.field.TypedField;
import tech.wetech.flexmodel.query.Direction;
import tech.wetech.flexmodel.query.Projections;
import tech.wetech.flexmodel.query.Query;
import tech.wetech.flexmodel.session.Session;
import tech.wetech.flexmodel.session.SessionFactory;
import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static tech.wetech.flexmodel.query.expr.Expressions.field;

/**
 * @author cjbi
 */
public abstract class FlexmodelAbstractDataFetcher<T> implements DataFetcher<T> {

  protected final String schemaName;
  protected final String modelName;
  protected final SessionFactory sessionFactory;

  protected static final JsonObjectConverter jsonObjectConverter = new JacksonObjectConverter();

  protected static final String PAGE_NUMBER = "page";
  protected static final String PAGE_SIZE = "size";
  protected static final String ORDER_BY = "order_by";
  protected static final String WHERE = "where";
  protected static final String ID = "id";
  protected static final String AFFECTED_ROWS = "affected_rows";
  protected static final String AGG_COUNT = "_count";
  protected static final String AGG_MAX = "_max";
  protected static final String AGG_MIN = "_min";
  protected static final String AGG_SUM = "_sum";
  protected static final String AGG_AVG = "_avg";
  protected static final String[] AGG_FIELDS = new String[]{AGG_COUNT, AGG_MAX, AGG_MIN, AGG_SUM, AGG_AVG};


  public FlexmodelAbstractDataFetcher(String schemaName, String modelName, SessionFactory sessionFactory) {
    this.schemaName = schemaName;
    this.modelName = modelName;
    this.sessionFactory = sessionFactory;
  }

  protected List<Map<String, Object>> findRelationDataList(Session session, DataFetchingEnvironment env, String path, String modelName, RelationField relationField, Object id) {
    EntityDefinition entity = (EntityDefinition) session.getModel(relationField.getModelName());
    EntityDefinition targetEntity = (EntityDefinition) session.getModel(relationField.getFrom());
    path = path == null ? relationField.getName() : path + "/" + relationField.getName();
    List<SelectedField> selectedFields = env.getSelectionSet().getFields(path + "/*");
    List<RelationField> relationFields = new ArrayList<>();
    List<Map<String, Object>> list = session.find(entity.getName(), query -> query
      .select(projection -> {
        TypedField<?, ?> idField = entity.findIdField().orElseThrow();
        projection.addField(idField.getName(), Projections.field(entity.getName() + "." + idField.getName()));
        for (SelectedField selectedField : selectedFields) {
          TypedField<?, ?> flexModelField = targetEntity.getField(selectedField.getName());
          if (flexModelField == null) {
            continue;
          }
          if (flexModelField instanceof RelationField secondaryRelationField) {
            relationFields.add(secondaryRelationField);
            continue;
          }
          projection.addField(selectedField.getName(), Projections.field(targetEntity.getName() + "." + flexModelField.getName()));
        }
        return projection;
      })
      .leftJoin(joins -> joins.addLeftJoin(join -> join.setFrom(targetEntity.getName())))
      .where(field(entity.getName() + "." + entity.findIdField().map(TypedField::getName).orElseThrow()).eq(id))
    );
    List<Map<String, Object>> result = new ArrayList<>();
    for (Map<String, Object> map : list) {
      Map<String, Object> resultData = new HashMap<>(map);
      result.add(resultData);
      for (RelationField sencondaryRelationField : relationFields) {
        Object secondaryId = map.get(entity.findIdField().map(TypedField::getName).orElseThrow());
        List<Map<String, Object>> associationDataList = findRelationDataList(session, env, path, sencondaryRelationField.getFrom(), sencondaryRelationField, secondaryId);
        resultData.put(sencondaryRelationField.getName(), sencondaryRelationField.isMultiple() ?
          associationDataList : associationDataList.stream().findFirst().orElse(null));
      }
    }
    return result;
  }

  protected Query getQuery(Integer pageNumber, Integer pageSize, Map<String, String> orderBy, Map<String, Object> where, Query query) {
    if (pageSize != null && pageNumber != null) {
      query.page(pageNumber, pageSize);
    }
    if (orderBy != null) {
      query.orderBy(sort -> {
          orderBy.forEach((k, v) -> sort.addOrder(k, Direction.valueOf(v.toUpperCase())));
          return sort;
        }
      );
    }
    if (where != null) {
      query.where(jsonObjectConverter.toJsonString(where));
    }
    return query;
  }

  protected Map<String, Object> getArguments(DataFetchingEnvironment env) {
    Map<String, Object> newVariables = new HashMap<>();
    newVariables.putAll(env.getVariables());
    Map<String, Object> variables = env.getGraphQlContext()
      .getOrDefault("__VARIABLES", new ConcurrentHashMap<>());
    newVariables.putAll(variables);
    return ValuesResolver.getArgumentValues(
      env.getGraphQLSchema().getCodeRegistry(),
      env.getFieldDefinition().getArguments(),
      env.getField().getArguments(),
      CoercedVariables.of(newVariables),
      env.getGraphQlContext(),
      env.getLocale());
  }

  @SuppressWarnings("unchecked")
  protected <R> R getArgument(DataFetchingEnvironment env, String name) {
    return (R) getArguments(env).get(name);
  }

}
