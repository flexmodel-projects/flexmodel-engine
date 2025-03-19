package tech.wetech.flexmodel.mapping;

import java.time.LocalTime;

/**
 * @author cjbi
 */
public class TimeTypeHandler implements TypeHandler<LocalTime> {
  @Override
  public LocalTime convertParameter(tech.wetech.flexmodel.Field field, Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof LocalTime localTime) {
      return localTime;
    }
    return LocalTime.parse(value.toString());
  }
}
