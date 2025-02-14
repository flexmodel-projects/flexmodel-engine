package tech.wetech.flexmodel.dsl;

import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter;

import java.util.Map;

/**
 * @author cjbi
 */
public interface Expression {

  Map<String, Object> toMap();

  default String toJsonString() {
    return new JacksonObjectConverter().toJsonString(toMap());
  }

}
