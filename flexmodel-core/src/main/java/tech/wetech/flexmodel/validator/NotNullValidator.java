package tech.wetech.flexmodel.validator;

import tech.wetech.flexmodel.TypedField;

import java.util.Map;

/**
 * @author cjbi
 */
@SuppressWarnings({"all"})
public class NotNullValidator extends AbstractConstraintValidator {

  public static final NotNullValidator INSTANCE = new NotNullValidator();

  public NotNullValidator(String message) {
    super(message);
  }

  public NotNullValidator() {
    super("must not be null");
  }

  @Override
  public void validate(TypedField field, Object value) throws ConstraintValidException {
    if (value == null) {
      handleThrows(field, null, Map.of());
    }

  }

  @Override
  public boolean equals(Object obj) {
    return this.getClass().equals(obj.getClass());
  }

}
