package tech.wetech.flexmodel.validations;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cjbi
 */
public class DataValidException extends RuntimeException {

  private final List<ConstraintValidException> constraintValidExceptions;

  public DataValidException(List<ConstraintValidException> constraintValidExceptions) {
    super(constraintValidExceptions.stream()
      .map(e -> e.getField().getName() + " " + e.getMessage())
      .collect(Collectors.joining("; ")));
    this.constraintValidExceptions = constraintValidExceptions;
  }

  public List<ConstraintValidException> getConstraintValidExceptions() {
    return constraintValidExceptions;
  }
}
