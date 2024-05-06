package tech.wetech.flexmodel.validations;

import tech.wetech.flexmodel.TypedField;

/**
 * @author cjbi
 */
public class NumberRangeValidator<NUM extends Number> extends AbstractConstraintValidator<NUM> {

  private final int min;
  private final int max;

  public NumberRangeValidator(int min, int max) {
    super("must be between {{min}} and {{max}}");
    this.min = min;
    this.max = max;
  }

  public NumberRangeValidator(String message, int min, int max) {
    super(message);
    this.min = min;
    this.max = max;
  }

  @Override
  public void validate(TypedField<NUM, ?> field, NUM number) throws ConstraintValidException {
    double value;
    if (number == null || (value = number.doubleValue()) < min || value > max) {
      handleThrows(field, number);
    }
  }

  public int getMin() {
    return min;
  }

  public int getMax() {
    return max;
  }
}
