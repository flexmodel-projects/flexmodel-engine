package tech.wetech.flexmodel.supports.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author cjbi
 */
public class NumberMinValidatorMixIn {

  @JsonCreator
  public NumberMinValidatorMixIn(@JsonProperty("message") String message, @JsonProperty("min") Number min) {
  }

}
