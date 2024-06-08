package tech.wetech.flexmodel.supports.jackson.mixin;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.wetech.flexmodel.validations.*;

/**
 * @author cjbi
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = DateMaxValidator.class, name = "DateMaxValidator"),
  @JsonSubTypes.Type(value = DateMinValidator.class, name = "DateMinValidator"),
  @JsonSubTypes.Type(value = DateRangeValidator.class, name = "DateRangeValidator"),
  @JsonSubTypes.Type(value = DatetimeMaxValidator.class, name = "DatetimeMaxValidator"),
  @JsonSubTypes.Type(value = DatetimeMinValidator.class, name = "DatetimeMinValidator"),
  @JsonSubTypes.Type(value = DatetimeRangeValidator.class, name = "DatetimeRangeValidator"),
  @JsonSubTypes.Type(value = EmailValidator.class, name = "EmailValidator"),
  @JsonSubTypes.Type(value = NotNullValidator.class, name = "NotNullValidator"),
  @JsonSubTypes.Type(value = NumberMaxValidator.class, name = "NumberMaxValidator"),
  @JsonSubTypes.Type(value = NumberMinValidator.class, name = "NumberMinValidator"),
  @JsonSubTypes.Type(value = NumberRangeValidator.class, name = "NumberRangeValidator"),
  @JsonSubTypes.Type(value = RegexpValidator.class, name = "RegexpValidator"),
  @JsonSubTypes.Type(value = URLValidator.class, name = "URLValidator"),
})
public class ConstraintValidatorMixIn {


}
