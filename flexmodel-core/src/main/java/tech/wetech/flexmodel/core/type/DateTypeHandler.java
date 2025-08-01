package tech.wetech.flexmodel.core.type;

import tech.wetech.flexmodel.core.model.field.Field;

import java.time.LocalDate;

/**
 * @author cjbi
 */
public class DateTypeHandler implements TypeHandler<LocalDate> {
  @Override
  public LocalDate convertParameter(Field field, Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof LocalDate localDate) {
      return localDate;
    }
    return LocalDate.parse(value.toString());
  }

}
