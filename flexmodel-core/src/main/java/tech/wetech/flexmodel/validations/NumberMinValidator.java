package tech.wetech.flexmodel.validations;

import tech.wetech.flexmodel.TypedField;

/**
 * @author cjbi
 */
public class NumberMinValidator<NUM extends Number> extends AbstractConstraintValidator<NUM> {

  private final Number min;

  public NumberMinValidator(String message, Number min) {
    super(message);
    this.min = min;
  }

  public NumberMinValidator(Number min) {
    super("must be greater than or equal to {{min}}");
    this.min = min;
  }

  @Override
  public void validate(TypedField<NUM, ?> field, NUM number) throws ConstraintValidException {
    if (number == null || number.doubleValue() < min.doubleValue()) {
      handleThrows(field, number);
    }
  }

  public Number getMin() {
    return min;
  }

}
