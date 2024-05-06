package tech.wetech.flexmodel.jsonlogic.evaluator.sql;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cjbi
 * @date 2022/9/6
 */
public class IndexPlaceholderHandler implements PlaceholderHandler {

  private final List<Object> parameters = new ArrayList<>();

  @Override
  public String handle(String key, Object value) {
    parameters.add(value);
    return "?";
  }

  @Override
  public Object[] getParameters() {
    return parameters.toArray();
  }

}
