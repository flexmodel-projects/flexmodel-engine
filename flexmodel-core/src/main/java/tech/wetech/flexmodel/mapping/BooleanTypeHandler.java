package tech.wetech.flexmodel.mapping;

/**
 * @author cjbi
 */
public class BooleanTypeHandler implements TypeHandler<Boolean> {

  @Override
  public Boolean convertParameter(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Boolean bool) {
      return bool;
    }
    return Boolean.valueOf(value.toString());
  }

}
