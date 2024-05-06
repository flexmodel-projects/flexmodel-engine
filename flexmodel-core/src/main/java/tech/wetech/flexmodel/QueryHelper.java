package tech.wetech.flexmodel;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author cjbi
 */
public class QueryHelper {

  public static void validate(Query query) {
    Query.Projection projection = query.projection();
    Query.GroupBy groupBy = query.groupBy();
    if (groupBy != null) {
      Set<String> groupFields = groupBy.fields().stream()
        .map(Query.QueryField::name)
        .collect(Collectors.toSet());
      List<String> errorFields = new ArrayList<>();
      if (projection != null) {
        Map<String, Query.QueryCall> projectFields = projection.fields();
        for (Map.Entry<String, Query.QueryCall> entry : projectFields.entrySet()) {
          String key = entry.getKey();
          Query.QueryCall value = entry.getValue();
          if (value instanceof Query.QueryField queryField) {
            if (!groupFields.contains(key) && !groupFields.contains(queryField.name())) {
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
  }

  public static Map<String, AssociationField> findAssociationFields(Model model, Query query) {
    Map<String, AssociationField> associationFields = new HashMap<>();
    if (model instanceof Entity entity) {
      Query.Projection projection = query.projection();
      if (projection != null) {
        for (Map.Entry<String, Query.QueryCall> entry : projection.fields().entrySet()) {
          String key = entry.getKey();
          Query.QueryCall value = entry.getValue();
          if (value instanceof Query.QueryField queryField) {
            entity.fields().stream()
              .filter(f -> f.name().equals(queryField.name()) && f instanceof AssociationField)
              .map(f -> (AssociationField) f)
              .findFirst()
              .ifPresent(f -> associationFields.put(key, f));
          }
        }
      } else {
        for (Field field : model.fields()) {
          if (field instanceof AssociationField assField) {
            associationFields.put(assField.name(), assField);
          }
        }
      }
    }
    return associationFields;
  }

}
