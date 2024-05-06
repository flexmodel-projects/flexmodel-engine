package tech.wetech.flexmodel.validations;

/**
 * @author cjbi
 */
public class EmailValidator extends RegexpValidator {

  public EmailValidator() {
    this("not a well-formed email address");
  }

  public EmailValidator(String message) {
    super(message, "[\\w]+@[A-Za-z]+(\\.[A-Za-z0-9]+){1,2}");
  }
}
