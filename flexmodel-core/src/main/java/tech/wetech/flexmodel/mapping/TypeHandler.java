package tech.wetech.flexmodel.mapping;

/**
 * @author cjbi
 */
public interface TypeHandler<T> {

  T convertParameter(tech.wetech.flexmodel.Field field, Object value);

}
