package tech.wetech.flexmodel.jsonlogic.evaluator.expressions;

import tech.wetech.flexmodel.jsonlogic.ast.JsonLogicArray;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluationException;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluator;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicExpression;

import java.util.Collection;

/**
 * @author cjbi
 * @date 2022/11/7
 */
public class ContainsExpression implements JsonLogicExpression {

  public static final ContainsExpression CONTAINS = new ContainsExpression(false);
  public static final ContainsExpression NOT_CONTAINS = new ContainsExpression(true);
  private final boolean isNot;

  public ContainsExpression(boolean isNot) {
    this.isNot = isNot;
  }

  @Override
  public String key() {
    return isNot ? "not_contains" : "contains";
  }

  @Override
  public <T extends JsonLogicEvaluator> Boolean evaluate(T evaluator, JsonLogicArray arguments, Object data) throws JsonLogicEvaluationException {
    Object left = evaluator.evaluate(arguments.get(0), data);
    Object right = evaluator.evaluate(arguments.get(1), data);
    if (left instanceof String leftString && right instanceof String rightString) {
      return isNot != leftString.contains(rightString);
    }
    if (left instanceof Collection<?> leftList && right instanceof Collection<?> rightList) {
      return leftList.containsAll(rightList);
    }
    if (left instanceof Collection<?> leftList && right instanceof String rightString) {
      return leftList.contains(rightString);
    }
    throw new JsonLogicEvaluationException("unsupported comparison");
  }
}
