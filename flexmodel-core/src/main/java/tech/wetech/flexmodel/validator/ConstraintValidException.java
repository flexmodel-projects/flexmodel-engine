package tech.wetech.flexmodel.validator;

import tech.wetech.flexmodel.TypedField;

/**
 * @author cjbi
 */
public class ConstraintValidException extends Exception {

  private final TypedField<?, ?> field;
  private final Object value;

  public ConstraintValidException(TypedField<?, ?> field, Object value, String message) {
    super(message);
    this.field = field;
    this.value = value;
  }

  public ConstraintValidException(TypedField<?, ?> field, Object value, String message, Throwable cause) {
    super(message, cause);
    this.field = field;
    this.value = value;
  }

  public TypedField<?, ?> getField() {
    return field;
  }

  public Object getValue() {
    return value;
  }
}
