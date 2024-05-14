package tech.wetech.flexmodel.mapping;

/**
 * @author cjbi
 */
public interface TypeHandler<T> {

  T convertParameter(Object value);

}
