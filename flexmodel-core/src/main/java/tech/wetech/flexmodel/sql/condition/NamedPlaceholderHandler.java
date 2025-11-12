package tech.wetech.flexmodel.sql.condition;

import java.util.HashMap;
import java.util.Map;

/**
 * 以命名参数方式渲染占位符。
 */
public class NamedPlaceholderHandler implements PlaceholderHandler {

  private final Map<String, Object> parameters = new HashMap<>();
  private int placeholderIndex;

  @Override
  public String handle(String key, Object value) {
    String sanitized = key.replace(".", "_")
      .replaceAll("[\"'`]", "");
    String name = sanitized + "_" + placeholderIndex++;
    parameters.put(name, value);
    return ":" + name;
  }

  @Override
  public Map<String, Object> getParameters() {
    return parameters;
  }
}

