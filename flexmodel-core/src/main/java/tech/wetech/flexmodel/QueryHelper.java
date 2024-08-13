package tech.wetech.flexmodel;

import tech.wetech.flexmodel.sql.SqlExecutionException;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static tech.wetech.flexmodel.Projections.field;

/**
 * @author cjbi
 */
public class QueryHelper {

  public static void validate(AbstractSessionContext sessionContext, String modelName, Query query) {
    String schemaName = sessionContext.getSchemaName();
    MappedModels mappedModels = sessionContext.getMappedModels();
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
      Model model = mappedModels.getModel(schemaName, modelName);
      for (Query.Join join : query.getJoins().getJoins()) {
        if (join.getFrom() == null) {
          throw new SqlExecutionException("Join from model must not be null");
        }
        if (model instanceof Entity entity && join.getLocalField() == null && join.getForeignField() == null) {
          RelationField relationField = entity.findRelationByEntityName(join.getFrom())
            .orElseThrow();
          join.setLocalField(entity.findIdField().map(IDField::getName).orElseThrow());
          join.setForeignField(relationField.getTargetField());
        } else {
          if (join.getLocalField() == null || join.getForeignField() == null) {
            throw new SqlExecutionException("LocalField and foreignField must not be null when is not association field");
          }
        }
      }
    }
  }

  public static Map<String, RelationField> findRelationFields(Model model, Query query) {
    Map<String, RelationField> relationFields = new HashMap<>();
    if (model instanceof Entity entity) {
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

  private static List<Map<String, Object>> findRelationList(AbstractSessionContext sessionContext,
                                                            BiFunction<String, Query,
                                                              List<Map<String, Object>>> relationFn,
                                                            RelationField relationField, Object id) {
    Model model = sessionContext.getModel(relationField.getModelName());
    if (model instanceof Entity entity) {
      List<Map<String, Object>> mapList = relationFn.apply(entity.getName(),
        new Query()
          .setProjection(projection -> {
              Entity targetEntity = (Entity) sessionContext.getModel(relationField.getTargetEntity());
              for (TypedField<?, ?> field : targetEntity.getFields()) {
                if (field instanceof RelationField) {
                  continue;
                }
                projection.addField(field.getName(), field(field.getModelName() + "." + field.getName()));
              }
              return projection;
            }
          )
          .setJoins(joins -> joins.addInnerJoin(join -> join.setFrom(relationField.getTargetEntity())))
          .setFilter(f -> f.equalTo(entity.getName() + "." + entity.findIdField().orElseThrow().getName(),
            id instanceof String ? "\"" + id + "\"" : id))
      );
      return mapList;
    }
    return List.of();
  }

  public static void deepQuery(List<Map<String, Object>> parentList,
                               BiFunction<String, Query,
                                 List<Map<String, Object>>> relationFn,
                               Model model,
                               Query query,
                               AbstractSessionContext sessionContext,
                               int maxDepth) {
    deepQuery(parentList, relationFn, model, query, sessionContext, new AtomicInteger(maxDepth));
  }


  private static void deepQuery(List<Map<String, Object>> parentList,
                                BiFunction<String, Query,
                                  List<Map<String, Object>>> relationFn,
                                Model model,
                                Query query,
                                AbstractSessionContext sessionContext,
                                AtomicInteger maxDepth) {
    if (maxDepth.get() <= 0) {
      return;
    }
    Map<String, RelationField> relationFieldMap = QueryHelper.findRelationFields(model, query);
    relationFieldMap.forEach((key, relationField) ->
      parentList.parallelStream().forEach(item -> {
        if (model instanceof Entity entity) {
          IDField idField = entity.findIdField().orElseThrow();
          Object id = item.get(idField.getName());
          List<Map<String, Object>> list = findRelationList(sessionContext, relationFn, relationField, id);
          maxDepth.decrementAndGet();
          deepQuery(list, relationFn, sessionContext.getModel(relationField.getTargetEntity()), null, sessionContext, maxDepth);
          if (!list.isEmpty()) {
            item.put(key, relationField.getCardinality() == RelationField.Cardinality.ONE_TO_ONE ? list.getFirst() : list);
          }
        }
      }));
  }

}
