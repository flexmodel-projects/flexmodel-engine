package tech.wetech.flexmodel.jsonlogic.evaluator.expressions;

import tech.wetech.flexmodel.jsonlogic.ast.JsonLogicArray;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluationException;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluator;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicExpression;

/**
 * @author cjbi
 * @date 2022/11/7
 */
public class EndsWithExpression implements JsonLogicExpression {

  public static final EndsWithExpression INSTANCE = new EndsWithExpression();

  @Override
  public String key() {
    return "ends_with";
  }

  @Override
  public <T extends JsonLogicEvaluator> Boolean evaluate(T evaluator, JsonLogicArray arguments, Object data) throws JsonLogicEvaluationException {
    Object left = evaluator.evaluate(arguments.get(0), data);
    Object right = evaluator.evaluate(arguments.get(1), data);
    if (left instanceof String leftString && right instanceof String rightString) {
      return leftString.endsWith(rightString);
    }
    throw new JsonLogicEvaluationException("unsupported comparison");
  }
}
