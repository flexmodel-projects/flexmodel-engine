package tech.wetech.flexmodel.jsonlogic.evaluator.sql;

/**
 * @author cjbi
 * @date 2022/9/6
 */
public interface PlaceholderHandler {

  String handle(String key, Object value);

  Object getParameters();

}
