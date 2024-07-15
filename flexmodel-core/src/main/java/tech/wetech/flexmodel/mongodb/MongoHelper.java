package tech.wetech.flexmodel.mongodb;

import org.bson.Document;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.graph.JoinGraphNode;

import java.util.*;

import static tech.wetech.flexmodel.Query.Join.JoinType.INNER_JOIN;
import static tech.wetech.flexmodel.RelationField.Cardinality.MANY_TO_MANY;

/**
 * @author cjbi
 */
class MongoHelper {

  static List<Document> createPipeline(String modelName, MongoContext mongoContext, Query query) {
    QueryHelper.validate(mongoContext, modelName, query);
    String schemaName = mongoContext.getSchemaName();
    Model model = mongoContext.getMappedModels().getModel(schemaName, modelName);
    PhysicalNamingStrategy physicalNamingStrategy = mongoContext.getPhysicalNamingStrategy();
    List<Document> pipeline = new ArrayList<>();
    // 匹配条件
    if (query.getFilter() != null) {
      String bsonCondition = getMongoCondition(mongoContext, query.getFilter());
      pipeline.add(Document.parse(String.format("{ $match: %s }", bsonCondition)));
    }
    // 排序
    if (query.getSort() != null) {
      Document orders = new Document();
      for (Query.Sort.Order order : query.getSort().getOrders()) {
        orders.put(order.getField().getFieldName(), order.getDirection() == Direction.ASC ? 1 : -1);
        pipeline.add(new Document("$sort", orders));
      }
    }
    // 分页
    if (query.getOffset() != null) {
      pipeline.add(new Document("$skip", query.getOffset()));
    }
    if (query.getLimit() != null) {
      pipeline.add(new Document("$limit", query.getLimit()));
    }
    // 添加关联
    if (query.getJoins() != null) {
      for (Query.Join join : query.getJoins().getJoins()) {
        String joinCollectionName = physicalNamingStrategy.toPhysicalTableName(join.getFrom());
        Document lookup = new Document();
        RelationField relationField;
        if (model instanceof Entity entity
            && (relationField = entity.findRelationByEntityName(join.getFrom()).orElse(null)) != null
            && relationField.getCardinality() == MANY_TO_MANY
        ) {
          Entity targetEntity = (Entity) mongoContext.getMappedModels().getModel(mongoContext.getSchemaName(), relationField.getTargetEntity());
          JoinGraphNode joinGraphNode = new JoinGraphNode(entity, targetEntity, relationField);
          Document exchangeLookup = new Document();
          exchangeLookup.put("from", joinGraphNode.getJoinName());
          exchangeLookup.put("localField", entity.findIdField().map(IDField::getName).orElseThrow());
          exchangeLookup.put("foreignField", joinGraphNode.getJoinFieldName());
          exchangeLookup.put("as", joinGraphNode.getJoinName());
          pipeline.add(new Document("$lookup", exchangeLookup));

          lookup.put("from", joinCollectionName);
          lookup.put("localField", joinGraphNode.getJoinName() + "." + joinGraphNode.getInverseJoinFieldName());
          lookup.put("foreignField", join.getForeignField());
          lookup.put("as", join.getFrom());

        } else {
          lookup.put("from", joinCollectionName);
          lookup.put("localField", join.getLocalField());
          lookup.put("foreignField", join.getForeignField());
          lookup.put("as", join.getFrom());
        }

        if (join.getFilter() != null) {
          lookup.put("pipeline", List.of(Document.parse(String.format("{ $match: %s }", getMongoCondition(mongoContext, join.getFilter())))));
        }
        pipeline.add(new Document("$lookup", lookup));
        // mongo lookup默认就是left join
        if (join.getJoinType() == INNER_JOIN) {
          pipeline.add(new Document("$match", Map.of(join.getFrom(), Map.of("$ne", List.of())
          )));
        }

        pipeline.add(new Document("$unwind", "$" + join.getFrom()));
      }
    }

    Document project = new Document();
    project.put("_id", false);
    Map<String, RelationField> relationFields = QueryHelper.findRelationFields(model, query);
    boolean hasAggFunc = false;
    Query.Projection projection = query.getProjection();
    if (projection != null) {
      for (Map.Entry<String, Query.QueryCall> entry : projection.getFields().entrySet()) {
        String key = entry.getKey();
        if (relationFields.containsKey(key)) {
          // 不查关联字段
          continue;
        }
        if (entry.getValue() instanceof Query.AggFunc) {
          hasAggFunc = true;
          project.put(key, true);
          continue;
        }
        if (entry.getValue() instanceof Query.QueryField queryField) {
          project.put(key, "$" + getNameSimply(model, queryField));
        }
      }
    } else {
      for (Field field : model.getFields()) {
        if (relationFields.containsKey(field.getName())) {
          // 不查关联字段
          continue;
        }
        project.put(field.getName(), true);
      }
    }

    if (hasAggFunc) {
      Document group = new Document();
      Map<String, Object> groupId = new HashMap<>();
      if (query.getGroupBy() == null) {
        group.put("_id", null);
      } else {
        Query.GroupBy groupBy = query.getGroupBy();
        for (Query.QueryField field : groupBy.getFields()) {
          groupId.put(field.getFieldName(), "$" + field.getFieldName());
        }
        group.put("_id", groupId);
      }

      for (Map.Entry<String, Query.QueryCall> entry : projection.getFields().entrySet()) {
        String key = entry.getKey();
        Query.QueryCall value = entry.getValue();
        if (value instanceof Query.QueryFunc aggFunc) {
          String name = null;
          if (aggFunc.getArgs() != null && aggFunc.getArgs().length > 0 && aggFunc.getArgs()[0] instanceof Query.QueryField field) {
            name = field.getName();
          }
          switch (aggFunc.getOperator()) {
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
              String fmt = (String) aggFunc.getArgs()[1];
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
            case null, default -> throw new IllegalStateException("Unexpected value: " + aggFunc.getOperator());
          }
        } else if (value instanceof Query.QueryField field) {
          group.put(field.getFieldName(), Map.of("$first", "$" + field.getFieldName()));
        }
      }
      pipeline.add(new Document("$group", group));
    }

    // 投影字段
    pipeline.add(new Document("$project", project));
    return pipeline;
  }

  private static String getNameSimply(Model model, Query.QueryField queryField) {
    String modelName = queryField.getModelName();
    String fieldName = queryField.getFieldName();
    if (model.getName().equals(modelName)) {
      return fieldName;
    }
    return queryField.getName();
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
