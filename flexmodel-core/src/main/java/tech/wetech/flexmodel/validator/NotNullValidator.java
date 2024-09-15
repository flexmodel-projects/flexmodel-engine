package tech.wetech.flexmodel.validator;

import tech.wetech.flexmodel.IDField;
import tech.wetech.flexmodel.TypedField;

import java.util.Map;

import static tech.wetech.flexmodel.IDField.GeneratedValue.AUTO_INCREMENT;

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
    if (field instanceof IDField idField) {
      if (idField.getGeneratedValue() == AUTO_INCREMENT) {
        return;
      }
    }
    if (value == null) {
      handleThrows(field, null, Map.of());
    }

  }

  @Override
  public boolean equals(Object obj) {
    return this.getClass().equals(obj.getClass());
  }

}
