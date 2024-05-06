package tech.wetech.flexmodel;

/**
 * @author cjbi
 */
public class JsonTypeHandler implements TypeHandler<Object> {
  @Override
  public Object convertParameter(Object value) {
    return value;
  }
}
