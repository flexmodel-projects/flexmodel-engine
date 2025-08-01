package tech.wetech.flexmodel.core.type;

import tech.wetech.flexmodel.core.model.field.Field;

/**
 * @author cjbi
 */
public class DecimalTypeHandler implements TypeHandler<Double> {
  @Override
  public Double convertParameter(Field field, Object value) {
    if (value instanceof Number number) {
      return number.doubleValue();
    }
    return null;
  }
}
