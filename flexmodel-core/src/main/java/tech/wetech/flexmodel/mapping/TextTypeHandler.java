package tech.wetech.flexmodel.mapping;

/**
 * @author cjbi
 */
public class TextTypeHandler implements TypeHandler<String> {

  @Override
  public String convertParameter(tech.wetech.flexmodel.Field field, Object value) {
    if (value == null) {
      return null;
    }
    return value.toString();
  }

}
