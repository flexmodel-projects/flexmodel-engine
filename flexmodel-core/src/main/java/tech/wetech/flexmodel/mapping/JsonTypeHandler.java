package tech.wetech.flexmodel.mapping;

import tech.wetech.flexmodel.JsonObjectConverter;

/**
 * @author cjbi
 */
public class JsonTypeHandler implements TypeHandler<Object> {

  protected final JsonObjectConverter jsonObjectConverter;

    public JsonTypeHandler(JsonObjectConverter jsonObjectConverter) {
        this.jsonObjectConverter = jsonObjectConverter;
    }

    @Override
  public Object convertParameter(tech.wetech.flexmodel.Field field, Object value) {
    return value;
  }
}
