package tech.wetech.flexmodel.condition;

import tech.wetech.flexmodel.JsonUtils;

import java.util.*;

/**
 * 将 DSL JSON 条件解析为内部条件语法树。
 */
public final class ConditionParser {

  public ConditionNode parse(String expression) {
    if (expression == null || expression.isBlank()) {
      return LogicalConditionNode.emptyAnd();
    }
    @SuppressWarnings("unchecked")
    Map<String, Object> map = JsonUtils.parseToObject(expression, Map.class);
    if (map == null || map.isEmpty()) {
      return LogicalConditionNode.emptyAnd();
    }
    ConditionNode node = parseMap(map);
    return node == null ? LogicalConditionNode.emptyAnd() : node;
  }

  private ConditionNode parseMap(Map<String, Object> map) {
    List<ConditionNode> nodes = new ArrayList<>();
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      if ("_and".equals(key) || "_or".equals(key)) {
        ConditionOperator operator = "_and".equals(key) ? ConditionOperator.AND : ConditionOperator.OR;
        nodes.add(parseLogicalList(asListOfMaps(value), operator));
      } else {
        ConditionNode node = parseFieldNode(key, value);
        if (node != null) {
          nodes.add(node);
        }
      }
    }
    if (nodes.isEmpty()) {
      return LogicalConditionNode.emptyAnd();
    }
    if (nodes.size() == 1) {
      return nodes.get(0);
    }
    return LogicalConditionNode.of(ConditionOperator.AND, nodes);
  }

  private ConditionNode parseLogicalList(List<Map<String, Object>> list, ConditionOperator operator) {
    if (list.isEmpty()) {
      return LogicalConditionNode.emptyAnd();
    }
    List<ConditionNode> children = new ArrayList<>(list.size());
    for (Map<String, Object> item : list) {
      ConditionNode node = parseMap(item);
      if (node != null && !node.isEmpty()) {
        children.add(node);
      }
    }
    if (children.isEmpty()) {
      return LogicalConditionNode.emptyAnd();
    }
    return LogicalConditionNode.of(operator, children);
  }

  private ConditionNode parseFieldNode(String fieldPath, Object value) {
    if (value instanceof Map<?, ?> valueMap) {
      return parseFieldMap(fieldPath, toOrderedMap(valueMap));
    }
    // 非 Map 值默认为等值匹配
    return new FieldConditionNode(fieldPath, ConditionOperator.EQ, value);
  }

  private ConditionNode parseFieldMap(String fieldPath, Map<String, Object> valueMap) {
    List<ConditionNode> nodes = new ArrayList<>();
    for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      if (key.startsWith("_")) {
        ConditionOperator operator = toOperator(key);
        nodes.add(new FieldConditionNode(fieldPath, operator, value));
      } else {
        String nestedPath = fieldPath + "." + key;
        ConditionNode nested = parseFieldNode(nestedPath, value);
        if (nested != null) {
          nodes.add(nested);
        }
      }
    }
    if (nodes.isEmpty()) {
      return null;
    }
    if (nodes.size() == 1) {
      return nodes.get(0);
    }
    return LogicalConditionNode.of(ConditionOperator.AND, nodes);
  }

  private ConditionOperator toOperator(String key) {
    return switch (key) {
      case "_eq" -> ConditionOperator.EQ;
      case "_ne" -> ConditionOperator.NE;
      case "_gt" -> ConditionOperator.GT;
      case "_gte" -> ConditionOperator.GTE;
      case "_lt" -> ConditionOperator.LT;
      case "_lte" -> ConditionOperator.LTE;
      case "_in" -> ConditionOperator.IN;
      case "_nin" -> ConditionOperator.NIN;
      case "_between" -> ConditionOperator.BETWEEN;
      case "_contains" -> ConditionOperator.CONTAINS;
      case "_not_contains" -> ConditionOperator.NOT_CONTAINS;
      case "_starts_with" -> ConditionOperator.STARTS_WITH;
      case "_ends_with" -> ConditionOperator.ENDS_WITH;
      default -> throw new IllegalArgumentException("Unsupported operator: " + key);
    };
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> asListOfMaps(Object value) {
    if (value == null) {
      return Collections.emptyList();
    }
    if (value instanceof List<?> list) {
      List<Map<String, Object>> result = new ArrayList<>(list.size());
      for (Object element : list) {
        if (element instanceof Map<?, ?> map) {
          result.add((Map<String, Object>) map);
        } else {
          throw new IllegalArgumentException("Logical operator expects object elements, got: " + element);
        }
      }
      return result;
    }
    throw new IllegalArgumentException("Logical operator expects array, got: " + value);
  }

  private Map<String, Object> toOrderedMap(Map<?, ?> valueMap) {
    Map<String, Object> ordered = new LinkedHashMap<>();
    for (Map.Entry<?, ?> entry : valueMap.entrySet()) {
      ordered.put(Objects.toString(entry.getKey(), ""), entry.getValue());
    }
    return ordered;
  }

  /**
   * 转换为可迭代集合，便于后续处理。
   */
  public static Collection<?> toCollection(Object value) {
    if (value == null) {
      return Collections.emptyList();
    }
    if (value instanceof Collection<?> collection) {
      return collection;
    }
    if (value.getClass().isArray()) {
      int length = java.lang.reflect.Array.getLength(value);
      List<Object> list = new ArrayList<>(length);
      for (int i = 0; i < length; i++) {
        list.add(java.lang.reflect.Array.get(value, i));
      }
      return list;
    }
    return Collections.singleton(value);
  }
}

