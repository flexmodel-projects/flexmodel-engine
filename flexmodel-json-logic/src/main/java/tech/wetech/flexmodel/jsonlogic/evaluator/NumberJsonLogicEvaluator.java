package tech.wetech.flexmodel.jsonlogic.evaluator;

import tech.wetech.flexmodel.jsonlogic.evaluator.expressions.MathExpression;
import tech.wetech.flexmodel.jsonlogic.evaluator.expressions.TableFieldExpression;

/**
 * @author cjbi
 * @date 2022/12/17
 */
public class NumberJsonLogicEvaluator extends PrimitiveTypeJsonLogicEvaluator implements JsonLogicEvaluator {

  public NumberJsonLogicEvaluator() {

    addOperation(MathExpression.ADD);
    addOperation(MathExpression.SUBTRACT);
    addOperation(MathExpression.MULTIPLY);
    addOperation(MathExpression.DIVIDE);
    addOperation(MathExpression.MODULO);
    addOperation(MathExpression.MIN);
    addOperation(MathExpression.MAX);
    addOperation(TableFieldExpression.INSTANCE);
  }

}
