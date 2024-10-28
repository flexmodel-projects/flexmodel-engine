package tech.wetech.flexmodel;

import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter;

import java.util.*;

/**
 * @author cjbi
 */
@SuppressWarnings("all")
public abstract class AbstractExpressionCalculator<T> implements ExpressionCalculator<T> {

  private static final Map<String, String> opMap = new HashMap<>();
  private static final JsonObjectConverter jsonObjectConverter = new JacksonObjectConverter();

  static {
    opMap.put("_and", "and");
    opMap.put("_or", "or");
    opMap.put("_eq", "==");
    opMap.put("_ne", "!=");
    opMap.put("_gt", ">");
    opMap.put("_lt", "<");
    opMap.put("_gte", ">=");
    opMap.put("_lte", "<=");
    opMap.put("_in", "in");
    opMap.put("_nin", "not_in");
    opMap.put("_contains", "contains");
    opMap.put("_not_contains", "not_contains");
    opMap.put("_starts_with", "starts_with");
    opMap.put("_ends_with", "ends_with");
    opMap.put("_between", "between");
  }

  protected String transform(String input) {
    Map<String, Object> inputMap = jsonObjectConverter.parseToMap(input);
    return jsonObjectConverter.toJsonString(transform(inputMap));
  }

  private Map<String, Object> transform(Map<String, Object> input) {
    List<Object> andList = new ArrayList<>();

    // 递归处理输入 Map，构造 "_and" 条件
    for (Map.Entry<String, Object> entry : input.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();

      if (key.equals("_and") || key.equals("_or")) {
        // 如果是 "_and" 或 "_or"，递归处理其内部条件
        List<Map<String, Object>> conditions = (List<Map<String, Object>>) value;
        List<Object> subList = new ArrayList<>();
        for (Map<String, Object> condition : conditions) {
          subList.add(transform(condition));
        }
        andList.add(Map.of(opMap.get(key), subList));
      } else if (value instanceof Map) {
        // 处理基本的字段条件
        Map<String, Object> conditionMap = (Map<String, Object>) value;
        for (String conditionKey : conditionMap.keySet()) {
          if (conditionKey.startsWith("_")) {
            andList.add(Map.of(opMap.get(conditionKey), Arrays.asList(Map.of("var", key), conditionMap.get(conditionKey))));
          }
        }
      }
    }

    // 如果只有一个条件，则直接返回该条件，不添加 "_and"
    if (andList.size() == 1) {
      return (Map<String, Object>) andList.get(0);
    }

    // 多条件时返回带 "_and" 的列表
    return Map.of(opMap.get("_and"), andList);
  }

}
