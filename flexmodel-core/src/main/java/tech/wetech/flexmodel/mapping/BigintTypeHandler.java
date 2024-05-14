package tech.wetech.flexmodel.mapping;

/**
 * @author cjbi
 */
public class BigintTypeHandler implements TypeHandler<Long> {

  @Override
  public Long convertParameter(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Number number) {
      return number.longValue();
    }
    return Long.valueOf(value.toString());
  }
}
