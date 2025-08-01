package tech.wetech.flexmodel.core.query.expr;

import tech.wetech.flexmodel.core.supports.jackson.JacksonObjectConverter;

import java.util.Map;

/**
 * @author cjbi
 */
public interface Expression {

  Map<String, Object> toMap();

  default String toJsonString() {
    Map<String, Object> map = toMap();
    if (map == null || map.isEmpty()) {
      return null;
    }
    return new JacksonObjectConverter().toJsonString(toMap());
  }

}
