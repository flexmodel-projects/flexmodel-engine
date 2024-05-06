package tech.wetech.flexmodel.mongodb;

import org.bson.Document;
import tech.wetech.flexmodel.*;

import java.util.*;

import static tech.wetech.flexmodel.Query.Join.JoinType.INNER_JOIN;

/**
 * @author cjbi
 */
class MongoHelper {

  static List<Document> createPipeline(String modelName, MongoContext mongoContext, Query query) {
    String schemaName = mongoContext.getSchemaName();
    Model model = mongoContext.getMappedModels().getModel(schemaName, modelName);
    PhysicalNamingStrategy physicalNamingStrategy = mongoContext.getPhysicalNamingStrategy();
    List<Document> pipeline = new ArrayList<>();
    // 匹配条件
    if (query.filter() != null) {
      String bsonCondition = getMongoCondition(mongoContext, query.filter());
      pipeline.add(Document.parse(String.format("{ $match: %s }", bsonCondition)));
    }
    // 排序
    if (query.sort() != null) {
      Document orders = new Document();
      for (Query.Sort.Order order : query.sort().orders()) {
        orders.put(order.field().fieldName(), order.direction() == Direction.ASC ? 1 : -1);
        pipeline.add(new Document("$sort", orders));
      }
    }
    // 分页
    if (query.offset() != null) {
      pipeline.add(new Document("$skip", query.offset()));
    }
    if (query.limit() != null) {
      pipeline.add(new Document("$limit", query.limit()));
    }
    // 添加关联
    if (query.joiners() != null) {
      for (Query.Join join : query.joiners().joins()) {
        String joinCollectionName = physicalNamingStrategy.toPhysicalTableName(join.from());
        Document lookup = new Document();
        lookup.put("from", joinCollectionName);
        lookup.put("localField", join.localField());
        lookup.put("foreignField", join.foreignField());
        lookup.put("as", join.from());
        if (join.filter() != null) {
          lookup.put("pipeline", List.of(Document.parse(String.format("{ $match: %s }", getMongoCondition(mongoContext, join.filter())))));
        }
        pipeline.add(new Document("$lookup", lookup));
        // mongo lookup默认就是left join
        if (join.joinType() == INNER_JOIN) {
          pipeline.add(new Document("$match", Map.of(join.from(), Map.of("$ne", List.of())
          )));
        }
        pipeline.add(new Document("$unwind", "$" + join.from()));
      }
    }

    Document project = new Document();
    project.put("_id", false);
    Map<String, AssociationField> associationFields = QueryHelper.findAssociationFields(model, query);
    boolean hasAggFunc = false;
    Query.Projection projection = query.projection();
    if (projection != null) {
      for (Map.Entry<String, Query.QueryCall> entry : projection.fields().entrySet()) {
        String key = entry.getKey();
        if (associationFields.containsKey(key)) {
          // 不查关联字段
          continue;
        }
        if (entry.getValue() instanceof Query.AggFunc) {
          hasAggFunc = true;
          project.put(key, true);
          continue;
        }
        if (entry.getValue() instanceof Query.QueryField queryField) {
          project.put(key, "$" + queryField.fieldName());
        }
      }
    } else {
      for (Field field : model.fields()) {
        if (associationFields.containsKey(field.name())) {
          // 不查关联字段
          continue;
        }
        project.put(field.name(), true);
      }
    }

    if (hasAggFunc) {
      Document group = new Document();
      Map<String, Object> groupId = new HashMap<>();
      if (query.groupBy() == null) {
        group.put("_id", null);
      } else {
        Query.GroupBy groupBy = query.groupBy();
        for (Query.QueryField field : groupBy.fields()) {
          groupId.put(field.fieldName(), "$" + field.fieldName());
        }
        group.put("_id", groupId);
      }

      for (Map.Entry<String, Query.QueryCall> entry : projection.fields().entrySet()) {
        String key = entry.getKey();
        Query.QueryCall value = entry.getValue();
        if (value instanceof Query.QueryFunc aggFunc) {
          String name = null;
          if (aggFunc.args() != null && aggFunc.args().length > 0 && aggFunc.args()[0] instanceof Query.QueryField field) {
            name = field.fieldName();
          }
          switch (aggFunc.operator()) {
            case "count" -> group.put(key, Map.of("$sum", 1));
            case "avg" -> group.put(key, Map.of("$avg", "$" + name));
            case "sum" -> group.put(key, Map.of("$sum", "$" + name));
            case "max" -> group.put(key, Map.of("$max", "$" + name));
            case "min" -> group.put(key, Map.of("$min", "$" + name));
            case "date_format" -> {
              Map<String, String> utcMap = Map.of(
                "YYYY|yyyy", "%Y",
                "MM", "%m",
                "DD|dd", "%d",
                "hh|HH", "%H",
                "mm", "%M",
                "ss|SS", "%S"
              );
              String fmt = (String) aggFunc.args()[1];
              for (Map.Entry<String, String> e : utcMap.entrySet()) {
                fmt = fmt.replaceAll(Objects.toString(e.getKey()), e.getValue());
              }
              Map<String, Map<String, Object>> dateToString = Map.of("$dateToString",
                Map.of("date", "$" + name,
                  "format", fmt)
              );
              groupId.put(name, dateToString);
              group.put(key, Map.of("$first", dateToString));
            }
            case "dayofyear" -> {
              Map<String, String> dayOfYear = Map.of("$dayOfYear", "$" + name);
              groupId.put(name, dayOfYear);
              group.put(key, Map.of("$first", dayOfYear));
            }
            case "dayofmonth" -> {
              Map<String, String> dayOfMonth = Map.of("$dayOfMonth", "$" + name);
              groupId.put(name, dayOfMonth);
              group.put(key, Map.of("$first", dayOfMonth));
            }
            case "dayofweek" -> {
              Map<String, String> dayOfWeek = Map.of("$dayOfWeek", "$" + name);
              groupId.put(name, dayOfWeek);
              group.put(key, Map.of("$first", dayOfWeek));
            }
            case null, default -> throw new IllegalStateException("Unexpected value: " + aggFunc.operator());
          }
        } else if (value instanceof Query.QueryField field) {
          group.put(field.fieldName(), Map.of("$first", "$" + field.fieldName()));
        }
      }
      pipeline.add(new Document("$group", group));
    }

    // 投影字段
    pipeline.add(new Document("$project", project));
    return pipeline;
  }

  static String getMongoCondition(MongoContext mongoContext, String condition) {
    try {
      ExpressionCalculator<String> expressionCalculator = mongoContext.getConditionCalculator();
      return expressionCalculator.calculate(condition, null);
    } catch (ExpressionCalculatorException e) {
      throw new RuntimeException(e);
    }
  }

}
