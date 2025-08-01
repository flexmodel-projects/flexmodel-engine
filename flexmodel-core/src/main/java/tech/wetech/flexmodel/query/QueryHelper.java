package tech.wetech.flexmodel.query;

import tech.wetech.flexmodel.model.EntityDefinition;
import tech.wetech.flexmodel.model.ModelDefinition;
import tech.wetech.flexmodel.model.field.Field;
import tech.wetech.flexmodel.model.field.RelationField;
import tech.wetech.flexmodel.model.field.TypedField;
import tech.wetech.flexmodel.session.AbstractSessionContext;
import tech.wetech.flexmodel.sql.SqlExecutionException;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static tech.wetech.flexmodel.query.expr.Expressions.field;

/**
 * @author cjbi
 */
public class QueryHelper {

  public static void validate(AbstractSessionContext sessionContext, String modelName, Query query) {
    Query.Projection projection = query.getProjection();
    Query.GroupBy groupBy = query.getGroupBy();
    if (groupBy != null) {
      Set<String> groupFields = groupBy.getFields().stream()
        .map(Query.QueryField::getName)
        .collect(Collectors.toSet());
      List<String> errorFields = new ArrayList<>();
      if (projection != null) {
        Map<String, Query.QueryCall> projectFields = projection.getFields();
        for (Map.Entry<String, Query.QueryCall> entry : projectFields.entrySet()) {
          String key = entry.getKey();
          Query.QueryCall value = entry.getValue();
          if (value instanceof Query.QueryField queryField) {
            if (!groupFields.contains(key) && !groupFields.contains(queryField.getName())) {
              errorFields.add(key);
            }
          }
        }
      } else {
        throw new RuntimeException("Group by error, projection is null");
      }
      if (!errorFields.isEmpty()) {
        throw new RuntimeException("The fields " + String.join(", ", errorFields) + " has not been grouped or aggregated");
      }
    }
    if (query.getJoins() != null) {
      ModelDefinition model = (ModelDefinition) sessionContext.getModel(modelName);
      for (Query.Join join : query.getJoins().getJoins()) {
        if (join.getFrom() == null) {
          throw new SqlExecutionException("Join from model must not be null");
        }
        if (model instanceof EntityDefinition entity && join.getLocalField() == null && join.getForeignField() == null) {
          RelationField relationField = entity.findRelationByModelName(join.getFrom())
            .orElseThrow();
          String localField = relationField.getLocalField() != null ?
            relationField.getLocalField() : entity.findIdField().map(TypedField::getName).orElseThrow();
          join.setLocalField(localField);
          join.setForeignField(relationField.getForeignField());
        } else {
          if (join.getLocalField() == null || join.getForeignField() == null) {
            throw new SqlExecutionException("LocalField and foreignField must not be null when is not association field");
          }
        }
        sessionContext.addAliasModelIfPresent(join.getAs(), (ModelDefinition) sessionContext.getModel(join.getFrom()));
      }
    }
  }

  public static Map<String, RelationField> findRelationFields(ModelDefinition model, Query query) {
    Map<String, RelationField> relationFields = new HashMap<>();
    if (model instanceof EntityDefinition entity) {
      if (query != null && query.getProjection() != null) {
        for (Map.Entry<String, Query.QueryCall> entry : query.getProjection().getFields().entrySet()) {
          String key = entry.getKey();
          Query.QueryCall value = entry.getValue();
          if (value instanceof Query.QueryField queryField) {
            entity.getFields().stream()
              .filter(f -> f.getName().equals(queryField.getName()) && f instanceof RelationField)
              .map(f -> (RelationField) f)
              .findFirst()
              .ifPresent(f -> relationFields.put(key, f));
          }
        }
      } else {
        for (Field field : model.getFields()) {
          if (field instanceof RelationField assField) {
            relationFields.put(assField.getName(), assField);
          }
        }
      }
    }
    return relationFields;
  }

  private static List<Map<String, Object>> findRelationList(BiFunction<String, Query, List<Map<String, Object>>> relationFn, RelationField relationField, Set<Object> ids) {
    return relationFn.apply(relationField.getFrom(), new Query().where(field(relationField.getForeignField()).in(ids)));
  }

  public static void nestedQuery(List<Map<String, Object>> parentList,
                                 BiFunction<String, Query,
                                   List<Map<String, Object>>> relationFn,
                                 ModelDefinition model,
                                 Query query,
                                 AbstractSessionContext sessionContext,
                                 int maxDepth) {
    nestedQuery(parentList, relationFn, model, query, sessionContext, new AtomicInteger(maxDepth));
  }


  private static void nestedQuery(List<Map<String, Object>> parentList,
                                  BiFunction<String, Query,
                                    List<Map<String, Object>>> relationFn,
                                  ModelDefinition model,
                                  Query query,
                                  AbstractSessionContext sessionContext,
                                  AtomicInteger maxDepth) {
    if (maxDepth.get() <= 0) {
      return;
    }
    Map<String, RelationField> relationFieldMap = QueryHelper.findRelationFields(model, query);
    relationFieldMap.entrySet().parallelStream().forEach(entry -> {
      String key = entry.getKey();
      RelationField relationField = entry.getValue();
      // 获取本地字段的值
      Set<Object> ids = parentList.stream()
        .map(item -> item.get(relationField.getLocalField()))
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
      Map<Object, List<Map<String, Object>>> relationDataGroup = findRelationList(relationFn, relationField, ids).stream()
        .collect(Collectors.groupingBy(e -> e.get(relationField.getForeignField())));

      parentList.forEach(item -> {
        if (model instanceof EntityDefinition) {
          Object id = item.get(relationField.getLocalField());
          if (id == null) {
            item.put(key, relationField.isMultiple() ? List.of() : null);
            return;
          }
          List<Map<String, Object>> list = relationDataGroup.getOrDefault(id, List.of());
          maxDepth.decrementAndGet();
          nestedQuery(list, relationFn, (ModelDefinition) sessionContext.getModel(relationField.getFrom()), null, sessionContext, maxDepth);
          Object value = relationField.isMultiple() ? list : (!list.isEmpty() ? list.getFirst() : null);
          item.put(key, value);
        }
      });
    });
  }

}
