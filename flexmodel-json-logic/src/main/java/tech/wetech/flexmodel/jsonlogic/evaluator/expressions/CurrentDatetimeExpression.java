package tech.wetech.flexmodel.jsonlogic.evaluator.expressions;

import tech.wetech.flexmodel.jsonlogic.ast.JsonLogicArray;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluationException;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluator;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicExpression;

import java.time.LocalDateTime;

/**
 * @author cjbi
 */
public class CurrentDatetimeExpression implements JsonLogicExpression {

  public static final CurrentDatetimeExpression INSTANCE = new CurrentDatetimeExpression();

  @Override
  public String key() {
    return "current_datetime";
  }

  @Override
  public <T extends JsonLogicEvaluator> Object evaluate(T evaluator, JsonLogicArray arguments, Object data) throws JsonLogicEvaluationException {
    return LocalDateTime.now();
  }
}
