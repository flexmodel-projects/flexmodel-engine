package tech.wetech.flexmodel.mongodb.condition;

import tech.wetech.flexmodel.JsonUtils;
import tech.wetech.flexmodel.condition.ConditionNode;
import tech.wetech.flexmodel.condition.ConditionOperator;
import tech.wetech.flexmodel.condition.FieldConditionNode;
import tech.wetech.flexmodel.condition.LogicalConditionNode;

import java.util.*;

import static tech.wetech.flexmodel.condition.ConditionParser.toCollection;

/**
 * 将条件语法树渲染为 Mongo 查询 JSON。
 */
public final class MongoConditionRenderer {

  private MongoConditionRenderer() {
  }

  public static String render(ConditionNode node) {
    Map<String, Object> document = renderNode(node);
    if (document.isEmpty()) {
      return "{}";
    }
    return JsonUtils.toJsonString(document);
  }

  private static Map<String, Object> renderNode(ConditionNode node) {
    if (node == null || node.isEmpty()) {
      return Map.of();
    }
    if (node instanceof LogicalConditionNode logical) {
      return renderLogical(logical);
    }
    if (node instanceof FieldConditionNode field) {
      return renderField(field);
    }
    throw new IllegalArgumentException("Unknown condition node: " + node);
  }

  private static Map<String, Object> renderLogical(LogicalConditionNode logical) {
    List<Map<String, Object>> children = new ArrayList<>();
    for (ConditionNode child : logical.getChildren()) {
      Map<String, Object> doc = renderNode(child);
      if (!doc.isEmpty()) {
        children.add(doc);
      }
    }
    if (children.isEmpty()) {
      return Map.of();
    }
    if (logical.getOperator() == ConditionOperator.AND) {
      if (children.size() == 1) {
        return children.get(0);
      }
      return Map.of("$and", children);
    }
    return Map.of("$or", children);
  }

  private static Map<String, Object> renderField(FieldConditionNode field) {
    String name = field.getFieldPath();
    Object value = field.getValue();
    return switch (field.getOperator()) {
      case EQ -> Map.of(name, Map.of("$eq", value));
      case NE -> Map.of(name, Map.of("$ne", value));
      case GT -> Map.of(name, Map.of("$gt", value));
      case GTE -> Map.of(name, Map.of("$gte", value));
      case LT -> Map.of(name, Map.of("$lt", value));
      case LTE -> Map.of(name, Map.of("$lte", value));
      case IN -> Map.of(name, Map.of("$in", new ArrayList<>(toCollection(value))));
      case NIN -> Map.of(name, Map.of("$nin", new ArrayList<>(toCollection(value))));
      case BETWEEN -> renderBetween(name, value);
      case CONTAINS -> renderRegex(name, value, "contains", false);
      case NOT_CONTAINS -> renderRegex(name, value, "contains", true);
      case STARTS_WITH -> renderRegex(name, value, "starts", false);
      case ENDS_WITH -> renderRegex(name, value, "ends", false);
      default -> throw new IllegalStateException("Unsupported operator for Mongo: " + field.getOperator());
    };
  }

  private static Map<String, Object> renderBetween(String name, Object value) {
    Collection<?> collection = toCollection(value);
    if (collection.size() != 2) {
      throw new IllegalArgumentException("_between operator expects exactly 2 values");
    }
    Object[] values = collection.toArray();
    Map<String, Object> ranges = new LinkedHashMap<>();
    ranges.put("$gte", values[0]);
    ranges.put("$lte", values[1]);
    return Map.of(name, ranges);
  }

  private static Map<String, Object> renderRegex(String name, Object value, String mode, boolean negate) {
    if (value instanceof Collection<?> collection) {
      if (collection.isEmpty()) {
        return Map.of();
      }
      String operator = negate ? "$nin" : "$in";
      return Map.of(name, Map.of(operator, new ArrayList<>(collection)));
    }
    if (value == null) {
      return Map.of();
    }
    String pattern = switch (mode) {
      case "starts" -> "^" + escapeRegex(value.toString()) + ".*";
      case "ends" -> ".*" + escapeRegex(value.toString()) + "$";
      case "contains" -> ".*" + escapeRegex(value.toString()) + ".*";
      default -> value.toString();
    };
    Map<String, Object> regex = Map.of("$regex", pattern);
    if (negate) {
      return Map.of(name, Map.of("$not", regex));
    }
    return Map.of(name, regex);
  }

  private static String escapeRegex(String value) {
    StringBuilder sb = new StringBuilder();
    for (char c : value.toCharArray()) {
      if ("\\.[]{}()*+-?^$|".indexOf(c) >= 0) {
        sb.append("\\");
      }
      sb.append(c);
    }
    return sb.toString();
  }
}

