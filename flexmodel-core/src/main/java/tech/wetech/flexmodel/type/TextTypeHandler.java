package tech.wetech.flexmodel.type;

import tech.wetech.flexmodel.model.field.Field;

/**
 * @author cjbi
 */
public class TextTypeHandler implements TypeHandler<String> {

  @Override
  public String convertParameter(Field field, Object value) {
    if (value == null) {
      return null;
    }
    return value.toString();
  }

}
