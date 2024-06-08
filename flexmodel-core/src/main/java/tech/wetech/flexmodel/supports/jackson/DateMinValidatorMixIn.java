package tech.wetech.flexmodel.supports.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * @author cjbi
 */
public class DateMinValidatorMixIn {

  @JsonCreator
  public DateMinValidatorMixIn(@JsonProperty("message") String message, @JsonProperty("min") LocalDate min) {
  }

}
