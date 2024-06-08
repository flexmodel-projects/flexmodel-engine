package tech.wetech.flexmodel.supports.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author cjbi
 */
public class IndexMixIn {

  @JsonCreator
  public IndexMixIn(@JsonProperty("modelName") String modelName, @JsonProperty("indexName") String indexName) {
  }

}
