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
  @JsonSubTypes.Type(value = IDField.class, name = "id"),
  @JsonSubTypes.Type(value = StringField.class, name = "string"),
  @JsonSubTypes.Type(value = TextField.class, name = "text"),
  @JsonSubTypes.Type(value = DecimalField.class, name = "decimal"),
  @JsonSubTypes.Type(value = IntField.class, name = "int"),
  @JsonSubTypes.Type(value = BigintField.class, name = "bigint"),
  @JsonSubTypes.Type(value = BooleanField.class, name = "boolean"),
  @JsonSubTypes.Type(value = DatetimeField.class, name = "datetime"),
  @JsonSubTypes.Type(value = DateField.class, name = "date"),
  @JsonSubTypes.Type(value = JsonField.class, name = "json"),
  @JsonSubTypes.Type(value = AssociationField.class, name = "association"),
})
public abstract class TypedFieldMixIn {

  @JsonCreator
  public TypedFieldMixIn(@JsonProperty("name") String name) {
  }
}
