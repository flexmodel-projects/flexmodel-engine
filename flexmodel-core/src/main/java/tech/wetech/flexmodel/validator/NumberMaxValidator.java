package tech.wetech.flexmodel.validator;

import tech.wetech.flexmodel.TypedField;

import java.util.Map;

/**
 * @author cjbi
 */
public class NumberMaxValidator<NUM extends Number> extends AbstractConstraintValidator<NUM> {

  private final Number max;

  public NumberMaxValidator(String message, Number max) {
    super(message);
    this.max = max;
  }

  public NumberMaxValidator(Number max) {
    super("must be less than or equal to ${max}");
    this.max = max;
  }

  @Override
  public void validate(TypedField<NUM, ?> field, NUM number) throws ConstraintValidException {
    if (number == null || number.doubleValue() > max.doubleValue()) {
      handleThrows(field, number, Map.of("max", max));
    }
  }

  public Number getMax() {
    return max;
  }
}
