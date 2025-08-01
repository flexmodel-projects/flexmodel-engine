package tech.wetech.flexmodel.core.sql.type;

/**
 * @author cjbi
 */
public class ResultMapException extends RuntimeException {

  public ResultMapException() {
  }

  public ResultMapException(String message) {
    super(message);
  }

  public ResultMapException(String message, Throwable cause) {
    super(message, cause);
  }
}
