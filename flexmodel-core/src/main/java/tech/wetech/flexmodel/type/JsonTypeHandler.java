package tech.wetech.flexmodel.type;

import tech.wetech.flexmodel.JsonObjectConverter;
import tech.wetech.flexmodel.model.field.Field;

/**
 * @author cjbi
 */
public class JsonTypeHandler implements TypeHandler<Object> {

  protected final JsonObjectConverter jsonObjectConverter;

    public JsonTypeHandler(JsonObjectConverter jsonObjectConverter) {
        this.jsonObjectConverter = jsonObjectConverter;
    }

    @Override
    public Object convertParameter(Field field, Object value) {
    return value;
  }
}
