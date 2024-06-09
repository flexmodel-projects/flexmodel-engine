package tech.wetech.flexmodel;

import tech.wetech.flexmodel.sql.SqlExecutionException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author cjbi
 */
public class QueryHelper {

  public static void validate(String schemaName, String modelName, MappedModels mappedModels, Query query) {
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
          throw new SqlExecutionException("join from model must not be null");
        }
        if (model instanceof Entity entity && join.getLocalField() == null && join.getForeignField() == null) {
          RelationField relationField = entity.findRelationByEntityName(join.getFrom())
            .orElseThrow();
          join.setLocalField(entity.getIdField().getName());
          join.setForeignField(relationField.getTargetField());
        } else {
          if (join.getLocalField() == null || join.getForeignField() == null) {
            throw new SqlExecutionException("localField and foreignField must not be null when is not association field");
          }
        }
      }
    }
  }

  public static Map<String, RelationField> findRelationFields(Model model, Query query) {
    Map<String, RelationField> relationFields = new HashMap<>();
    if (model instanceof Entity entity) {
      Query.Projection projection = query.getProjection();
      if (projection != null) {
        for (Map.Entry<String, Query.QueryCall> entry : projection.getFields().entrySet()) {
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

}
