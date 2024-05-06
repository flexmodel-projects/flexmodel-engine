package tech.wetech.flexmodel.validations;

import tech.wetech.flexmodel.TypedField;

/**
 * @author cjbi
 */
public class NumberMaxValidator<NUM extends Number> extends AbstractConstraintValidator<NUM> {

  private final int max;

  public NumberMaxValidator(String message, int max) {
    super(message);
    this.max = max;
  }

  public NumberMaxValidator(int max) {
    super("must be less than or equal to {{max}}");
    this.max = max;
  }

  @Override
  public void validate(TypedField<NUM, ?> field, NUM number) throws ConstraintValidException {
    if (number == null || number.doubleValue() > max) {
      handleThrows(field, number);
    }
  }

  public int getMax() {
    return max;
  }
}
