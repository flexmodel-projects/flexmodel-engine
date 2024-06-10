package tech.wetech.flexmodel.supports.jackson;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.wetech.flexmodel.generator.*;

/**
 * @author cjbi
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = DateNowValueGenerator.class, name = "DateNowValueGenerator"),
  @JsonSubTypes.Type(value = DatetimeNowValueGenerator.class, name = "DatetimeNowValueGenerator"),
  @JsonSubTypes.Type(value = FixedValueGenerator.class, name = "FixedValueGenerator"),
  @JsonSubTypes.Type(value = UUIDValueGenerator.class, name = "UUIDValueGenerator"),
  @JsonSubTypes.Type(value = ULIDValueGenerator.class, name = "ULIDValueGenerator"),
})
public class ValueGeneratorMixIn {
}
