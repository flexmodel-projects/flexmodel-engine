package tech.wetech.flexmodel.validator;

import tech.wetech.flexmodel.TypedField;

import java.time.LocalDateTime;

/**
 * @author cjbi
 */
public class DatetimeMinValidator extends AbstractConstraintValidator<LocalDateTime> {

  private final LocalDateTime min;

  public DatetimeMinValidator(LocalDateTime min) {
    this("must be greater than or equal to {{min}}", min);
  }

  public DatetimeMinValidator(String message, LocalDateTime min) {
    super(message);
    this.min = min;
  }

  @Override
  public void validate(TypedField<LocalDateTime, ?> field, LocalDateTime value) throws ConstraintValidException {
    if (value == null || value.isBefore(min)) {
      handleThrows(field, value);
    }
  }

  public LocalDateTime getMin() {
    return min;
  }
}
