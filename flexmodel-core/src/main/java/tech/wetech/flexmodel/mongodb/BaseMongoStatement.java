package tech.wetech.flexmodel.mongodb;

import org.bson.Document;
import tech.wetech.flexmodel.*;

import java.util.*;

import static tech.wetech.flexmodel.Query.Join.JoinType.INNER_JOIN;

/**
 * @author cjbi
 */
abstract class BaseMongoStatement {

  protected final MongoContext mongoContext;

  BaseMongoStatement(MongoContext mongoContext) {
    this.mongoContext = mongoContext;
  }

  private void addMatchStage(List<Document> pipeline, Query query) {
    if (query.getFilter() != null) {
      String bsonCondition = getMongoCondition(query.getFilter());
      pipeline.add(Document.parse(String.format("{ $match: %s }", bsonCondition)));
    }
  }

  private void addSortStage(List<Document> pipeline, Query query) {
    if (query.getSort() != null) {
      Document orders = new Document();
      for (Query.Sort.Order order : query.getSort().getOrders()) {
        orders.put(order.getField().getFieldName(), order.getDirection() == Direction.ASC ? 1 : -1);
      }
      pipeline.add(new Document("$sort", orders));
    }
  }

  private void addPaginationStages(List<Document> pipeline, Query query) {
    if (query.getPage() != null) {
      pipeline.add(new Document("$skip", query.getPage().getOffset()));
      pipeline.add(new Document("$limit", query.getPage().getPageSize()));
    }
  }

  private void addJoins(List<Document> pipeline, Model model,
                        PhysicalNamingStrategy physicalNamingStrategy, Query query) {
    if (query.getJoins() != null) {
      for (Query.Join join : query.getJoins().getJoins()) {
        String joinCollectionName = join.getFrom();
        Document lookup = createLookupDocument(pipeline, model, join, joinCollectionName);
        pipeline.add(new Document("$lookup", lookup));

        if (join.getJoinType() == INNER_JOIN) {
          pipeline.add(new Document("$match", Map.of(join.getFrom(), Map.of("$ne", List.of()))));
        }

        pipeline.add(new Document("$unwind", "$" + join.getFrom()));
      }
    }
  }

  private Document createLookupDocument(List<Document> pipeline, Model model, Query.Join join, String joinCollectionName) {
    Document lookup = new Document();
    lookup.append("from", joinCollectionName)
      .append("localField", join.getLocalField())
      .append("foreignField", join.getForeignField())
      .append("as", join.getFrom());

    if (join.getFilter() != null) {
      lookup.append("pipeline", List.of(Document.parse(String.format("{ $match: %s }", getMongoCondition(join.getFilter())))));
    }
    return lookup;
  }

  private void addProjectionStage(List<Document> pipeline, Model model, Query query) {
    Document project = new Document("_id", false);
    Map<String, RelationField> relationFields = QueryHelper.findRelationFields(model, query);
    boolean hasAggFunc = false;

    Query.Projection projection = query.getProjection();
    if (projection != null) {
      for (Map.Entry<String, Query.QueryCall> entry : projection.getFields().entrySet()) {
        String key = entry.getKey();
        if (relationFields.containsKey(key)) continue;

        Query.QueryCall value = entry.getValue();
        if (value instanceof Query.AggFunc) {
          hasAggFunc = true;
          project.put(key, true);
        } else if (value instanceof Query.QueryField queryField) {
          project.put(key, "$" + getNameSimply(model, queryField));
        }
      }
    } else {
      for (Field field : model.getFields()) {
        if (relationFields.containsKey(field.getName())) continue;
        project.put(field.getName(), true);
      }
    }

    if (hasAggFunc) {
      Document group = createGroupDocument(query, projection);
      pipeline.add(new Document("$group", group));
    }

    pipeline.add(new Document("$project", project));
  }

  private Document createGroupDocument(Query query, Query.Projection projection) {
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
        processAggregationFunction(group, groupId, key, aggFunc);
      } else if (value instanceof Query.QueryField field) {
        group.put(field.getFieldName(), Map.of("$first", "$" + field.getFieldName()));
      }
    }
    return group;
  }

  private void processAggregationFunction(Document group, Map<String, Object> groupId, String key, Query.QueryFunc aggFunc) {
    String name = getAggregationFieldName(aggFunc);
    switch (aggFunc.getOperator()) {
      case "count" -> group.put(key, Map.of("$sum", 1));
      case "avg" -> group.put(key, Map.of("$avg", "$" + name));
      case "sum" -> group.put(key, Map.of("$sum", "$" + name));
      case "max" -> group.put(key, Map.of("$max", "$" + name));
      case "min" -> group.put(key, Map.of("$min", "$" + name));
      case "date_format" -> {
        Document dateToString = createDateFormatAggregation(name, (String) aggFunc.getArgs()[1]);
        groupId.put(key, dateToString);
        group.put(key, Map.of("$first", dateToString));
      }
      case "dayofyear", "dayofmonth", "dayofweek" -> {
        Document dateOfYear = createDatePartAggregation(name, aggFunc.getOperator());
        groupId.put(name, dateOfYear);
        group.put(key, Map.of("$first", dateOfYear));
      }
      default -> throw new IllegalStateException("Unexpected value: " + aggFunc.getOperator());
    }
  }

  private String getAggregationFieldName(Query.QueryFunc aggFunc) {
    if (aggFunc.getArgs() != null && aggFunc.getArgs().length > 0 && aggFunc.getArgs()[0] instanceof Query.QueryField field) {
      return field.getName();
    }
    return null;
  }

  private Document createDateFormatAggregation(String fieldName, String format) {
    Map<String, String> utcMap = Map.of(
      "YYYY|yyyy", "%Y",
      "MM", "%m",
      "DD|dd", "%d",
      "hh|HH", "%H",
      "mm", "%M",
      "ss|SS", "%S"
    );
    for (Map.Entry<String, String> e : utcMap.entrySet()) {
      format = format.replaceAll(Objects.toString(e.getKey()), e.getValue());
    }
    return new Document("$dateToString", Map.of("date", "$" + fieldName, "format", format));
  }

  private Document createDatePartAggregation(String fieldName, String operator) {
    String mongoOperator = switch (operator) {
      case "dayofyear" -> "$dayOfYear";
      case "dayofmonth" -> "$dayOfMonth";
      case "dayofweek" -> "$dayOfWeek";
      default -> throw new IllegalStateException("Unexpected operator: " + operator);
    };
    return new Document(mongoOperator, "$" + fieldName);
  }

  protected List<Document> createPipeline(String modelName, Query query) {
    QueryHelper.validate(mongoContext, modelName, query);
    Model model = (Model) mongoContext.getModel(modelName);
    PhysicalNamingStrategy physicalNamingStrategy = mongoContext.getPhysicalNamingStrategy();
    List<Document> pipeline = new ArrayList<>();
    addMatchStage(pipeline, query);
    addSortStage(pipeline, query);
    addPaginationStages(pipeline, query);
    addJoins(pipeline, model, physicalNamingStrategy, query);
    addProjectionStage(pipeline, model, query);
    return pipeline;
  }

  private String getNameSimply(Model model, Query.QueryField queryField) {
    String aliasName = queryField.getAliasName();
    String fieldName = queryField.getFieldName();
    if (model.getName().equals(aliasName)) {
      return fieldName;
    }
    return queryField.getName();
  }

  protected String getMongoCondition(String condition) {
    try {
      ExpressionCalculator<String> expressionCalculator = mongoContext.getConditionCalculator();
      return expressionCalculator.calculate(condition, null);
    } catch (ExpressionCalculatorException e) {
      throw new RuntimeException(e);
    }
  }

}
