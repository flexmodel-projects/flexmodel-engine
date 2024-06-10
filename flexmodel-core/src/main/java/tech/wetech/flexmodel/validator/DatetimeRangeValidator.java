package tech.wetech.flexmodel.validator;

import tech.wetech.flexmodel.TypedField;

import java.time.LocalDateTime;

/**
 * @author cjbi
 */
public class DatetimeRangeValidator extends AbstractConstraintValidator<LocalDateTime> {

  private final LocalDateTime min;
  private final LocalDateTime max;

  public DatetimeRangeValidator(LocalDateTime min, LocalDateTime max) {
    this("must be between {{min}} and {{max}}", min, max);
  }

  public DatetimeRangeValidator(String message, LocalDateTime min, LocalDateTime max) {
    super(message);
    this.min = min;
    this.max = max;
  }

  @Override
  public void validate(TypedField<LocalDateTime, ?> field, LocalDateTime value) throws ConstraintValidException {
    if (value == null || (value.isBefore(min) || value.isAfter(max))) {
      handleThrows(field, value);
    }
  }

  public LocalDateTime getMin() {
    return min;
  }

  public LocalDateTime getMax() {
    return max;
  }
}
