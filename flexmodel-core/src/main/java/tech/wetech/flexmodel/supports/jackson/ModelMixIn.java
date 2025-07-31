package tech.wetech.flexmodel.supports.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.wetech.flexmodel.EntityDefinition;
import tech.wetech.flexmodel.EnumDefinition;
import tech.wetech.flexmodel.NativeQueryDefinition;

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
