package tech.wetech.flexmodel.supports.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * @author cjbi
 */
public class DatetimeRangeValidatorMixIn {

  @JsonCreator
  public DatetimeRangeValidatorMixIn(@JsonProperty("message") String message,
                                     @JsonProperty("min") LocalDateTime min,
                                     @JsonProperty("max") LocalDateTime max) {
  }

}
