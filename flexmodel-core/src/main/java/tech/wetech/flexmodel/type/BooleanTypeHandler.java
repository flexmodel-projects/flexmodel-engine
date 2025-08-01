package tech.wetech.flexmodel.type;

import tech.wetech.flexmodel.model.field.Field;

/**
 * @author cjbi
 */
public class BooleanTypeHandler implements TypeHandler<Boolean> {

  @Override
  public Boolean convertParameter(Field field, Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof Boolean bool) {
      return bool;
    }
    return Boolean.valueOf(value.toString());
  }

}
