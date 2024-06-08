package tech.wetech.flexmodel.supports.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * @author cjbi
 */
public class DateRangeValidatorMixIn {

  @JsonCreator
  public DateRangeValidatorMixIn(@JsonProperty("message") String message,
                                 @JsonProperty("min") LocalDate min,
                                 @JsonProperty("max") LocalDate max) {
  }

}
