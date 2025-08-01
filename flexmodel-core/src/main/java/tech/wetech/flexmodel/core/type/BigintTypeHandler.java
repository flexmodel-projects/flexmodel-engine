package tech.wetech.flexmodel.core.type;

import tech.wetech.flexmodel.core.model.field.Field;

/**
 * @author cjbi
 */
public class BigintTypeHandler implements TypeHandler<Long> {

  @Override
  public Long convertParameter(Field field, Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Number number) {
      return number.longValue();
    }
    return Long.valueOf(value.toString());
  }
}
