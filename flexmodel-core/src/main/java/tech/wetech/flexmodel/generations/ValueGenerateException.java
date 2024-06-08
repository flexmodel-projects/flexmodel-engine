package tech.wetech.flexmodel.generations;

import tech.wetech.flexmodel.TypedField;

import java.util.Map;

/**
 * @author cjbi
 */
public class ValueGenerateException extends RuntimeException {

  private final TypedField<?, ?> field;
  private final Object value;
  private final Map<String, Object> data;

  public ValueGenerateException(String message, TypedField<?, ?> field, Object value, Map<String, Object> data) {
    super(message);
    this.field = field;
    this.value = value;
    this.data = data;
  }

  public ValueGenerateException(String message, Throwable cause, TypedField<?, ?> field, Object value, Map<String, Object> data) {
    super(message, cause);
    this.field = field;
    this.value = value;
    this.data = data;
  }

  public TypedField<?, ?> getField() {
    return field;
  }

  public Object getValue() {
    return value;
  }

  public Map<String, Object> getData() {
    return data;
  }
}