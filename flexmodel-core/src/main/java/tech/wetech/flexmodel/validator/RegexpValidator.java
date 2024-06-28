package tech.wetech.flexmodel.validator;

import tech.wetech.flexmodel.TypedField;

import java.util.Map;

/**
 * @author cjbi
 */
public class RegexpValidator extends AbstractConstraintValidator<String> {

  private final String regexp;

  public RegexpValidator(String regexp) {
    super("must match ${regexp}");
    this.regexp = regexp;
  }

  public RegexpValidator(String message, String regexp) {
    super(message);
    this.regexp = regexp;
  }

  @Override
  public void validate(TypedField<String, ?> field, String value) throws ConstraintValidException {
    if (value == null || !value.matches(regexp)) {
      handleThrows(field, value, Map.of("regexp", regexp));
    }
  }

  public String getRegexp() {
    return regexp;
  }
}
