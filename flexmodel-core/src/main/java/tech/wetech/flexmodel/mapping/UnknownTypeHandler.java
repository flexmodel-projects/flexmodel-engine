package tech.wetech.flexmodel.mapping;

/**
 * @author cjbi
 */
public class UnknownTypeHandler implements TypeHandler<Object> {
  @Override
  public Object convertParameter(tech.wetech.flexmodel.Field field, Object value) {
    return value;
  }
}
