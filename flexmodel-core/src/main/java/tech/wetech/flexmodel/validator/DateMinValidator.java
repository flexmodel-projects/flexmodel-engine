package tech.wetech.flexmodel.validator;

import tech.wetech.flexmodel.TypedField;

import java.time.LocalDate;

/**
 * @author cjbi
 */
public class DateMinValidator extends AbstractConstraintValidator<LocalDate> {

  private final LocalDate min;

  public DateMinValidator(LocalDate min) {
    this("must be greater than or equal to {{min}}", min);
  }

  public DateMinValidator(String message, LocalDate min) {
    super(message);
    this.min = min;
  }

  @Override
  public void validate(TypedField<LocalDate, ?> field, LocalDate value) throws ConstraintValidException {
    if (value == null || value.isBefore(min)) {
      handleThrows(field, value);
    }
  }

  public LocalDate getMin() {
    return min;
  }
}
