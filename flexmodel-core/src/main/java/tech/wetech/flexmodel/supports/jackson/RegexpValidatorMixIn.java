package tech.wetech.flexmodel.supports.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author cjbi
 */
public class RegexpValidatorMixIn {

  @JsonCreator
  public RegexpValidatorMixIn(@JsonProperty("message") String message,
                              @JsonProperty("regexp") String regexp) {
  }

}
