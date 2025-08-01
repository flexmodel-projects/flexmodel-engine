package tech.wetech.flexmodel.core.type;

import tech.wetech.flexmodel.core.model.field.Field;

/**
 * @author cjbi
 */
public class UnknownTypeHandler implements TypeHandler<Object> {
  @Override
  public Object convertParameter(Field field, Object value) {
    return value;
  }
}
