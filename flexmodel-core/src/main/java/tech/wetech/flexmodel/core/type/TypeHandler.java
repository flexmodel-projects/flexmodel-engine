package tech.wetech.flexmodel.core.type;

import tech.wetech.flexmodel.core.model.field.Field;

/**
 * @author cjbi
 */
public interface TypeHandler<T> {

  T convertParameter(Field field, Object value);

}
