package tech.wetech.flexmodel.mapping;

/**
 * @author cjbi
 */
public class IntTypeHandler implements TypeHandler<Integer> {
  @Override
  public Integer convertParameter(tech.wetech.flexmodel.Field field, Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Number number) {
      return number.intValue();
    }
    return Integer.valueOf(value.toString());
  }
}
