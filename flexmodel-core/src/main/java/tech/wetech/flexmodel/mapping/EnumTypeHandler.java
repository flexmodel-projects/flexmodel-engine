package tech.wetech.flexmodel.mapping;

/**
 * @author cjbi
 */
public class EnumTypeHandler implements TypeHandler<Object> {
  @Override
  public Object convertParameter(tech.wetech.flexmodel.Field field, Object value) {
    return value;
  }
}
