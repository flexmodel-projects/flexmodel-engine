package tech.wetech.flexmodel.mapping;

/**
 * @author cjbi
 */
public class DecimalTypeHandler implements TypeHandler<Double> {
  @Override
  public Double convertParameter(tech.wetech.flexmodel.Field field, Object value) {
    if (value instanceof Number number) {
      return number.doubleValue();
    }
    return null;
  }
}
