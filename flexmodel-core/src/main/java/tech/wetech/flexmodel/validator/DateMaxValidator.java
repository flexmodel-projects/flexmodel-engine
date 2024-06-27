package tech.wetech.flexmodel.validator;

import tech.wetech.flexmodel.TypedField;

import java.time.LocalDate;
import java.util.Map;

/**
 * @author cjbi
 */
public class DateMaxValidator extends AbstractConstraintValidator<LocalDate> {

  private final LocalDate max;

  public DateMaxValidator(LocalDate max) {
    this("must be greater than or equal to {{max}}", max);
  }

  public DateMaxValidator(String message, LocalDate max) {
    super(message);
    this.max = max;
  }

  @Override
  public void validate(TypedField<LocalDate, ?> field, LocalDate value) throws ConstraintValidException {
    if (value == null || value.isAfter(max)) {
      handleThrows(field, value, Map.of("max", max));
    }
  }

  public LocalDate getMax() {
    return max;
  }
}
