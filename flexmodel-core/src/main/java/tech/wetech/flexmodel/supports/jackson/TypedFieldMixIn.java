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
  @JsonSubTypes.Type(value = IDField.class, name = "ID"),
  @JsonSubTypes.Type(value = StringField.class, name = "STRING"),
  @JsonSubTypes.Type(value = TextField.class, name = "TEXT"),
  @JsonSubTypes.Type(value = DecimalField.class, name = "DECIMAL"),
  @JsonSubTypes.Type(value = IntField.class, name = "INT"),
  @JsonSubTypes.Type(value = BigintField.class, name = "BIGINT"),
  @JsonSubTypes.Type(value = BooleanField.class, name = "BOOLEAN"),
  @JsonSubTypes.Type(value = DatetimeField.class, name = "DATETIME"),
  @JsonSubTypes.Type(value = DateField.class, name = "DATE"),
  @JsonSubTypes.Type(value = JsonField.class, name = "JSON"),
  @JsonSubTypes.Type(value = RelationField.class, name = "RELATION"),
  @JsonSubTypes.Type(value = EnumField.class, name = "ENUM"),
})
public abstract class TypedFieldMixIn {

  @JsonCreator
  public TypedFieldMixIn(@JsonProperty("name") String name) {
  }
}
