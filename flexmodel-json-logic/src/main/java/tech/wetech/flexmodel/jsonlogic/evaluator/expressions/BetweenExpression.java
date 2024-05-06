package tech.wetech.flexmodel.jsonlogic.evaluator.expressions;

import tech.wetech.flexmodel.jsonlogic.ast.JsonLogicArray;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluationException;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluator;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicExpression;

import java.time.LocalDateTime;

/**
 * @author cjbi
 */
public class BetweenExpression implements JsonLogicExpression {

  public static final BetweenExpression INSTANCE = new BetweenExpression();

  @Override
  public String key() {
    return "between";
  }

  @Override
  public <T extends JsonLogicEvaluator> Object evaluate(T evaluator, JsonLogicArray arguments, Object data) throws JsonLogicEvaluationException {
    if (arguments.size() != 3) {
      throw new JsonLogicEvaluationException("between expressions expect exactly 3 arguments");
    }
    Object value = evaluator.evaluate(arguments.get(0), data);
    Object left = evaluator.evaluate(arguments.get(1), data);
    Object right = evaluator.evaluate(arguments.get(2), data);
    if (value instanceof Number numValue
      && left instanceof Number minValue
      && right instanceof Number maxValue) {
      return minValue.longValue() <= numValue.longValue() &&
        numValue.longValue() <= maxValue.longValue();
    }
    if (value instanceof LocalDateTime timeValue
      && left instanceof LocalDateTime minValue
      && right instanceof LocalDateTime maxValue
    ) {
      return timeValue.isAfter(minValue) && timeValue.isBefore(maxValue);
    }
    return false;
  }
}
