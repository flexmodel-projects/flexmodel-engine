package tech.wetech.flexmodel.supports.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * @author cjbi
 */
public class DateMaxValidatorMixIn {

  @JsonCreator
  public DateMaxValidatorMixIn(@JsonProperty("message") String message, @JsonProperty("max") LocalDate max) {
  }

}
