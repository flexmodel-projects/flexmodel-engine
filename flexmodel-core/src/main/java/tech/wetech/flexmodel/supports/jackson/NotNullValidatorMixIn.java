package tech.wetech.flexmodel.supports.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author cjbi
 */
public class NotNullValidatorMixIn {

  @JsonCreator
  public NotNullValidatorMixIn(@JsonProperty("message") String message) {
  }
}
