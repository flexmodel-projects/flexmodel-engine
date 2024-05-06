package tech.wetech.flexmodel.jsonlogic.evaluator.expressions;

import tech.wetech.flexmodel.jsonlogic.ast.JsonLogicArray;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluationException;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluator;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicExpression;

import java.util.List;

/**
 * @author cjbi
 */
public class InExpression implements JsonLogicExpression {

  public static final InExpression NOT_IN = new InExpression(true);

  public static final InExpression IN = new InExpression(false);

  private final boolean isNot;

  public InExpression(boolean isNot) {
    this.isNot = isNot;
  }

  @Override
  public String key() {
    return isNot ? "not_in" : "in";
  }

  @Override
  public <T extends JsonLogicEvaluator> Object evaluate(T evaluator, JsonLogicArray arguments, Object data) throws JsonLogicEvaluationException {
    Object left = evaluator.evaluate(arguments.get(0), data);
    Object right = evaluator.evaluate(arguments.get(1), data);

    if (left instanceof List<?> leftList && right instanceof List<?> rightList) {
      return leftList.containsAll(rightList);
    }
    if (left instanceof List<?> leftList && right instanceof String rightString) {
      return leftList.contains(rightString);
    }
    if (left instanceof String leftString && right instanceof List<?> rightList) {
      return isNot != rightList.contains(leftString);
    }
    throw new JsonLogicEvaluationException("unsupported comparison");
  }
}
