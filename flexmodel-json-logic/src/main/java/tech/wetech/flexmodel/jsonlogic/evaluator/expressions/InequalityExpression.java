package tech.wetech.flexmodel.jsonlogic.evaluator.expressions;

import tech.wetech.flexmodel.jsonlogic.ast.JsonLogicArray;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluationException;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluator;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicExpression;

/**
 * @author cjbi
 * @date 2022/9/6
 */
public class InequalityExpression implements JsonLogicExpression {
  public static final InequalityExpression INSTANCE = new InequalityExpression(EqualityExpression.INSTANCE);

  private final EqualityExpression delegate;

  private InequalityExpression(EqualityExpression delegate) {
    this.delegate = delegate;
  }

  @Override
  public String key() {
    return "!=";
  }

  @Override
  public Object evaluate(JsonLogicEvaluator evaluator, JsonLogicArray arguments, Object data)
    throws JsonLogicEvaluationException {
    boolean result = (boolean) delegate.evaluate(evaluator, arguments, data);
    return !result;
  }
}

