package tech.wetech.flexmodel.supports.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.wetech.flexmodel.*;

/**
 * @author cjbi
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = IDField.class, name = ScalarType.ID_TYPE),
  @JsonSubTypes.Type(value = StringField.class, name = ScalarType.STRING_TYPE),
  @JsonSubTypes.Type(value = IntField.class, name = ScalarType.INT_TYPE),
  @JsonSubTypes.Type(value = LongField.class, name = ScalarType.LONG_TYPE),
  @JsonSubTypes.Type(value = FloatField.class, name = ScalarType.FLOAT_TYPE),
  @JsonSubTypes.Type(value = DateField.class, name = ScalarType.DATE_TYPE),
  @JsonSubTypes.Type(value = DateTimeField.class, name = ScalarType.DATETIME_TYPE),
  @JsonSubTypes.Type(value = TimeField.class, name = ScalarType.TIME_TYPE),
  @JsonSubTypes.Type(value = BooleanField.class, name = ScalarType.BOOLEAN_TYPE),
  @JsonSubTypes.Type(value = JSONField.class, name = ScalarType.JSON_TYPE),
  @JsonSubTypes.Type(value = RelationField.class, name = ScalarType.RELATION_TYPE),
  @JsonSubTypes.Type(value = EnumField.class, name = ScalarType.ENUM_TYPE),
})
public abstract class TypedFieldMixIn {

  @JsonCreator
  public TypedFieldMixIn(@JsonProperty("name") String name) {
  }
}
