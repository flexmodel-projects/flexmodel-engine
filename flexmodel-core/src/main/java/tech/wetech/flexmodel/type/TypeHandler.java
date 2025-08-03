package tech.wetech.flexmodel.type;

import tech.wetech.flexmodel.model.field.Field;

/**
 * @author cjbi
 */
public interface TypeHandler<T> {

  T convertParameter(Field field, Object value);

}
