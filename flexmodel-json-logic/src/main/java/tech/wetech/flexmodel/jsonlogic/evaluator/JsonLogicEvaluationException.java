package tech.wetech.flexmodel.jsonlogic.evaluator;

import tech.wetech.flexmodel.jsonlogic.JsonLogicException;

/**
 * @author cjbi
 * @date 2022/9/5
 */
public class JsonLogicEvaluationException extends JsonLogicException {

  public JsonLogicEvaluationException(String message) {
    super(message);
  }

  public JsonLogicEvaluationException(String message, Throwable cause) {
    super(message, cause);
  }

  public JsonLogicEvaluationException(Throwable cause) {
    super(cause);
  }
}
