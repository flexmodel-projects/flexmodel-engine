package tech.wetech.flexmodel.supports.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author cjbi
 */
public class ModelMixIn {

  @JsonCreator
  public ModelMixIn(@JsonProperty("name") String name) {
  }

}
