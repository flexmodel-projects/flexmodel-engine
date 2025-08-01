package tech.wetech.flexmodel.core.supports.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.wetech.flexmodel.core.model.EntityDefinition;
import tech.wetech.flexmodel.core.model.EnumDefinition;
import tech.wetech.flexmodel.core.model.NativeQueryDefinition;

/**
 * @author cjbi
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = EntityDefinition.class, name = "ENTITY"),
  @JsonSubTypes.Type(value = NativeQueryDefinition.class, name = "NATIVE_QUERY"),
  @JsonSubTypes.Type(value = EnumDefinition.class, name = "ENUM"),
})
public class ModelMixIn {

  @JsonCreator
  public ModelMixIn(@JsonProperty("name") String name) {
  }

}
