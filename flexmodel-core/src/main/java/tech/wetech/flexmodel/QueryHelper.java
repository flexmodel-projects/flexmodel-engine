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
      Model model = (Model) sessionContext.getModel(modelName);
      for (Query.Join join : query.getJoins().getJoins()) {
        if (join.getFrom() == null) {
          throw new SqlExecutionException("Join from model must not be null");
        }
        if (model instanceof Entity entity && join.getLocalField() == null && join.getForeignField() == null) {
          RelationField relationField = entity.findRelationByModelName(join.getFrom())
            .orElseThrow();
          String localField = relationField.getLocalField() != null ?
            relationField.getLocalField() : entity.findIdField().map(IDField::getName).orElseThrow();
          join.setLocalField(localField);
          join.setForeignField(relationField.getForeignField());
        } else {
          if (join.getLocalField() == null || join.getForeignField() == null) {
            throw new SqlExecutionException("LocalField and foreignField must not be null when is not association field");
          }
        }
        sessionContext.addAliasModelIfPresent(join.getAs(), (Model) sessionContext.getModel(join.getFrom()));
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
                                                            RelationField relationField, List<Object> ids) {
    Model model = (Model) sessionContext.getModel(relationField.getModelName());
    if (model instanceof Entity entity) {
      return relationFn.apply(entity.getName(),
        new Query()
          .withProjection(projection -> {
              Entity from = (Entity) sessionContext.getModel(relationField.getFrom());
              for (TypedField<?, ?> field : from.getFields()) {
                if (field instanceof RelationField) {
                  continue;
                }
                projection.addField(field.getName(), field(relationField.getFrom() + "_rel" + "." + field.getName()));
                sessionContext.addAliasModelIfPresent(relationField.getFrom() + "_rel", from);
              }
              return projection;
            }
          )
          .withJoin(joins -> joins.addInnerJoin(join -> join
              .setFrom(relationField.getFrom())
              .setAs(relationField.getFrom() + "_rel")
              .setLocalField(relationField.getLocalField())
              .setForeignField(relationField.getForeignField())
            )
          )
          .withFilter(f -> f.in(entity.getName() + "." + relationField.getLocalField(), ids))
      );
    }
    return List.of();
  }

  public static void nestedQuery(List<Map<String, Object>> parentList,
                                 BiFunction<String, Query,
                                   List<Map<String, Object>>> relationFn,
                                 Model model,
                                 Query query,
                                 AbstractSessionContext sessionContext,
                                 int maxDepth) {
    nestedQuery(parentList, relationFn, model, query, sessionContext, new AtomicInteger(maxDepth));
  }


  private static void nestedQuery(List<Map<String, Object>> parentList,
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
    relationFieldMap.entrySet().parallelStream().forEach(entry -> {
      String key = entry.getKey();
      RelationField relationField = entry.getValue();
      List<Object> ids = parentList.stream()
        .map(item -> item.get(relationField.getLocalField()))
        .filter(Objects::nonNull)
        .toList();
      Map<Object, List<Map<String, Object>>> relationDataGroup = findRelationList(sessionContext, relationFn, relationField, ids).stream()
        .collect(Collectors.groupingBy(e -> e.get(relationField.getForeignField())));

      parentList.forEach(item -> {
        if (model instanceof Entity entity) {
          Object id = item.get(relationField.getLocalField());
          if (id == null) {
            item.put(key, relationField.isMultiple() ? List.of() : null);
            return;
          }
          List<Map<String, Object>> list = relationDataGroup.getOrDefault(id, List.of());
          maxDepth.decrementAndGet();
          nestedQuery(list, relationFn, (Model) sessionContext.getModel(relationField.getFrom()), null, sessionContext, maxDepth);
          Object value = relationField.isMultiple() ? list : (!list.isEmpty() ? list.getFirst() : null);
          item.put(key, value);
        }
      });
    });
  }

}
