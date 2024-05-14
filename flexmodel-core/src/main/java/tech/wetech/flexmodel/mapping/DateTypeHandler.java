package tech.wetech.flexmodel.mapping;

import java.time.LocalDate;

/**
 * @author cjbi
 */
public class DateTypeHandler implements TypeHandler<LocalDate> {
  @Override
  public LocalDate convertParameter(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof LocalDate localDate) {
      return localDate;
    }
    return LocalDate.parse(value.toString());
  }

}
