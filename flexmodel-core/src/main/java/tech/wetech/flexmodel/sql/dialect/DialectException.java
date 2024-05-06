package tech.wetech.flexmodel.sql.dialect;

/**
 * An exception that occurs while reading mapping sources .
 *
 * @author cjbi@outlook.com
 */
public class DialectException extends RuntimeException {

  public DialectException(String message) {
    super(message);
  }

  public DialectException(Throwable cause) {
    super(cause);
  }

  public DialectException(String message, Throwable cause) {
    super(message, cause);
  }
}
