package tech.wetech.flexmodel;

/**
 * @author cjbi
 */
public interface TypeHandler<T> {

  T convertParameter(Object value);

}
