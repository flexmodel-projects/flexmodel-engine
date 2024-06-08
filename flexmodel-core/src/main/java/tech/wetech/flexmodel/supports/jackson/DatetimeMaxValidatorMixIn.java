package tech.wetech.flexmodel.supports.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * @author cjbi
 */
public class DatetimeMaxValidatorMixIn {

  @JsonCreator
  public DatetimeMaxValidatorMixIn(@JsonProperty("message") String message, @JsonProperty("max") LocalDateTime max) {
  }

}
