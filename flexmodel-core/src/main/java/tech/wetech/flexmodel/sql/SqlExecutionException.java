package tech.wetech.flexmodel.sql;

/**
 * @author cjbi
 */
public class SqlExecutionException extends RuntimeException {

  public enum Status {
    DEFAULT,
    INTEGRITY_CONSTRAINT_VIOLATION,
  }

  private final Status status;

  public Status getStatus() {
    return status;
  }

  public SqlExecutionException() {
    this.status = Status.DEFAULT;
  }

  public SqlExecutionException(String message) {
    super(message);
    this.status = Status.DEFAULT;
  }

  public SqlExecutionException(String message, Throwable cause) {
    super(message, cause);
    this.status = Status.DEFAULT;
  }

  public SqlExecutionException(String message, Throwable cause, Status status) {
    super(message, cause);
    this.status = Status.DEFAULT;
  }

}
