package tech.wetech.flexmodel.core.type;

import tech.wetech.flexmodel.core.model.field.Field;

/**
 * @author cjbi
 */
public class IntTypeHandler implements TypeHandler<Integer> {
  @Override
  public Integer convertParameter(Field field, Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Number number) {
      return number.intValue();
    }
    return Integer.valueOf(value.toString());
  }
}
