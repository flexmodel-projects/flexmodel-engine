package tech.wetech.flexmodel.supports.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.wetech.flexmodel.Entity;
import tech.wetech.flexmodel.Enum;
import tech.wetech.flexmodel.NativeQueryModel;

/**
 * @author cjbi
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = Entity.class, name = "ENTITY"),
  @JsonSubTypes.Type(value = NativeQueryModel.class, name = "NATIVE_QUERY"),
  @JsonSubTypes.Type(value = Enum.class, name = "ENUM"),
})
public class ModelMixIn {

  @JsonCreator
  public ModelMixIn(@JsonProperty("name") String name) {
  }

}
