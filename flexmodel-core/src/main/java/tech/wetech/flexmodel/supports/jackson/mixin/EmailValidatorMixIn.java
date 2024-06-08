package tech.wetech.flexmodel.supports.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author cjbi
 */
public class EmailValidatorMixIn {

  @JsonCreator
  public EmailValidatorMixIn(@JsonProperty("message") String message) {
  }

}
