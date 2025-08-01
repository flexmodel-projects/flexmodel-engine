package tech.wetech.flexmodel.core.type;

import tech.wetech.flexmodel.core.model.field.Field;

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
