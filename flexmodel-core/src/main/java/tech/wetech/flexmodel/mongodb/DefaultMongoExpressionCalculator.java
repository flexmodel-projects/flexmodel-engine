package tech.wetech.flexmodel.mongodb;

import tech.wetech.flexmodel.AbstractExpressionCalculator;
import tech.wetech.flexmodel.ExpressionCalculatorException;
import tech.wetech.flexmodel.condition.ConditionNode;
import tech.wetech.flexmodel.mongodb.condition.MongoConditionRenderer;

import java.util.Map;

/**
 * @author cjbi
 */
public class DefaultMongoExpressionCalculator extends AbstractExpressionCalculator<String> {

  @Override
  public String calculate(String expression, Map<String, Object> dataMap) throws ExpressionCalculatorException {
    if (expression == null) {
      throw new ExpressionCalculatorException("Expression is null");
    }
    try {
      ConditionNode conditionNode = parseCondition(expression);
      return MongoConditionRenderer.render(conditionNode);
    } catch (RuntimeException e) {
      throw new ExpressionCalculatorException(e.getMessage(), e);
    }
  }

}
