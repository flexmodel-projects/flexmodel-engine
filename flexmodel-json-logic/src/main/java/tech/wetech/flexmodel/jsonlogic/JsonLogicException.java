package tech.wetech.flexmodel.jsonlogic;

/**
 * @author cjbi
 * @date 2022/9/4
 */
public class JsonLogicException extends Exception {
  private JsonLogicException() {

  }

  public JsonLogicException(String message) {
    super(message);
  }

  public JsonLogicException(String message, Throwable cause) {
    super(message, cause);
  }

  public JsonLogicException(Throwable cause) {
    super(cause);
  }

  public JsonLogicException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
