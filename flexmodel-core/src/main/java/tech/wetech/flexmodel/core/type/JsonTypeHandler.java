package tech.wetech.flexmodel.core.type;

import tech.wetech.flexmodel.core.JsonObjectConverter;
import tech.wetech.flexmodel.core.model.field.Field;

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
