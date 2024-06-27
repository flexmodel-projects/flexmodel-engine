package tech.wetech.flexmodel.validator;

import tech.wetech.flexmodel.TypedField;

import java.util.Map;

/**
 * @author cjbi
 */
public class NumberRangeValidator<NUM extends Number> extends AbstractConstraintValidator<NUM> {

  private final Number min;
  private final Number max;

  public NumberRangeValidator(Number min, Number max) {
    super("must be between {{min}} and {{max}}");
    this.min = min;
    this.max = max;
  }

  public NumberRangeValidator(String message, Number min, Number max) {
    super(message);
    this.min = min;
    this.max = max;
  }

  @Override
  public void validate(TypedField<NUM, ?> field, NUM number) throws ConstraintValidException {
    double value;
    if (number == null || (value = number.doubleValue()) < min.doubleValue() || value > max.doubleValue()) {
      handleThrows(field, number, Map.of("min", min, "max", max));
    }
  }

  public Number getMin() {
    return min;
  }

  public Number getMax() {
    return max;
  }
}
