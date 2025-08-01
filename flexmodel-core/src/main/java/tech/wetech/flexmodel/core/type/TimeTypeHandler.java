package tech.wetech.flexmodel.core.type;

import tech.wetech.flexmodel.core.model.field.Field;

import java.time.LocalTime;

/**
 * @author cjbi
 */
public class TimeTypeHandler implements TypeHandler<LocalTime> {
  @Override
  public LocalTime convertParameter(Field field, Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof LocalTime localTime) {
      return localTime;
    }
    return LocalTime.parse(value.toString());
  }
}
