package tech.wetech.flexmodel.core.type;

import tech.wetech.flexmodel.core.model.field.Field;

/**
 * @author cjbi
 */
public class StringTypeHandler implements TypeHandler<String> {
  @Override
  public String convertParameter(Field field, Object value) {
    if (value == null) {
      return null;
    }
    return value.toString();
  }
}
