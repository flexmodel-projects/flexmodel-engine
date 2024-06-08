package tech.wetech.flexmodel.supports.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author cjbi
 */
public class NumberMaxValidatorMixIn {

  @JsonCreator
  public NumberMaxValidatorMixIn(@JsonProperty("message") String message, @JsonProperty("max") Number max) {
  }

}
