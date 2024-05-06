package tech.wetech.flexmodel.sql.type;

/**
 * @author cjbi
 */
public class TypeException extends RuntimeException {

  public TypeException() {
  }

  public TypeException(String message) {
    super(message);
  }

  public TypeException(String message, Throwable cause) {
    super(message, cause);
  }
}
