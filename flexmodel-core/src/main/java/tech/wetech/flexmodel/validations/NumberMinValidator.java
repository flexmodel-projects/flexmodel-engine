package tech.wetech.flexmodel.validations;

import tech.wetech.flexmodel.TypedField;

/**
 * @author cjbi
 */
public class NumberMinValidator<NUM extends Number> extends AbstractConstraintValidator<NUM> {

  private final int min;

  public NumberMinValidator(String message, int min) {
    super(message);
    this.min = min;
  }

  public NumberMinValidator(int min) {
    super("must be greater than or equal to {{min}}");
    this.min = min;
  }

  @Override
  public void validate(TypedField<NUM, ?> field, NUM number) throws ConstraintValidException {
    if (number == null || number.doubleValue() < min) {
      handleThrows(field, number);
    }
  }

  public int getMin() {
    return min;
  }

}
