package tech.wetech.flexmodel.jsonlogic.evaluator;

import tech.wetech.flexmodel.jsonlogic.evaluator.expressions.*;

/**
 * @author cjbi
 * @date 2022/9/4
 */
public class BooleanJsonLogicEvaluator extends PrimitiveTypeJsonLogicEvaluator implements JsonLogicEvaluator {

  public BooleanJsonLogicEvaluator() {

    addOperation(LogicExpression.AND);
    addOperation(LogicExpression.OR);

    addOperation(EqualityExpression.INSTANCE);
    addOperation(InequalityExpression.INSTANCE);

    addOperation(ComparisonExpression.GT);
    addOperation(ComparisonExpression.GTE);
    addOperation(ComparisonExpression.LT);
    addOperation(ComparisonExpression.LTE);

    addOperation(TableFieldExpression.INSTANCE);

    addOperation(ContainsExpression.CONTAINS);
    addOperation(ContainsExpression.NOT_CONTAINS);
    addOperation(StartsWithExpression.INSTANCE);
    addOperation(EndsWithExpression.INSTANCE);
    addOperation(BetweenExpression.INSTANCE);

    addOperation(RadioExpression.INSTANCE);
    addOperation(DatetimeExpression.INSTANCE);
    addOperation(MultipleExpression.INSTANCE);
    addOperation(AttachExpression.INSTANCE);
    addOperation(IdentifierExpression.INSTANCE);

    addOperation(InExpression.IN);
    addOperation(InExpression.NOT_IN);
  }

}
