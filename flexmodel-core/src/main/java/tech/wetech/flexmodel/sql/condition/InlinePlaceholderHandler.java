package tech.wetech.flexmodel.sql.condition;

/**
 * 在 SQL 中直接内联参数值。
 */
public class InlinePlaceholderHandler implements PlaceholderHandler {

  @Override
  public String handle(String key, Object value) {
    if (value == null) {
      return "null";
    }
    if (value instanceof Number || value instanceof Boolean) {
      return value.toString();
    }
    String text = value.toString().replace("'", "''");
    return "'" + text + "'";
  }
}

