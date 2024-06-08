package tech.wetech.flexmodel.supports.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author cjbi
 */
public class NumberRangeValidatorMixIn {

  @JsonCreator
  public NumberRangeValidatorMixIn(@JsonProperty("message") String message,
                                   @JsonProperty("min") Number min,
                                   @JsonProperty("max") Number max) {
  }
}
