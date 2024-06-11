package tech.wetech.flexmodel.sql;

/**
 * @author cjbi
 */
public class SqlExecutionException extends RuntimeException {


  public SqlExecutionException(String message) {
    super(message);
  }

  public SqlExecutionException(String message, Throwable cause) {
    super(message, cause);
  }

}
