package tech.wetech.flexmodel.type;

import tech.wetech.flexmodel.model.field.Field;

/**
 * @author cjbi
 */
public class EnumTypeHandler implements TypeHandler<Object> {
  @Override
  public Object convertParameter(Field field, Object value) {
    return value;
  }
}
