package tech.wetech.flexmodel.mapping;

import java.time.LocalDateTime;

/**
 * @author cjbi
 */
public class DatetimeTypeHandler implements TypeHandler<LocalDateTime> {
  @Override
  public LocalDateTime convertParameter(tech.wetech.flexmodel.Field field, Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof LocalDateTime localDateTime) {
      return localDateTime;
    }
    return LocalDateTime.parse(value.toString());
  }
}
