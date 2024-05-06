package tech.wetech.flexmodel.jsonlogic.evaluator.sql;

/**
 * @author cjbi
 */
public class IncludeValuePlaceholderHandler implements PlaceholderHandler {

  @Override
  public String handle(String key, Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Number || value instanceof Boolean) {
      return value.toString();
    }
    return String.format("'%s'", value);
  }

  @Override
  public Object getParameters() {
    return new Object();
  }
}
