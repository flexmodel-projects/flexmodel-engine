package tech.wetech.flexmodel.supports.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author cjbi
 */
public class DefaultValueGeneratorMixIn {

  @JsonCreator
  public DefaultValueGeneratorMixIn(@JsonProperty("value") Object value) {
  }

}
