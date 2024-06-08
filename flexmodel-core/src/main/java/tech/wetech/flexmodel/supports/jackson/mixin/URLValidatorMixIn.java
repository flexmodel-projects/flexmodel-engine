package tech.wetech.flexmodel.supports.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author cjbi
 */
public class URLValidatorMixIn {

  @JsonCreator
  public URLValidatorMixIn(@JsonProperty("message") String message) {
  }
}
