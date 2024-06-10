package tech.wetech.flexmodel.supports.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author cjbi
 */
public class FixedValueGeneratorMixIn {

  @JsonCreator
  public FixedValueGeneratorMixIn(@JsonProperty("value") Object value) {
  }

}
