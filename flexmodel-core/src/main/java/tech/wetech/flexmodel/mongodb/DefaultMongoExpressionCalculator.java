package tech.wetech.flexmodel.mongodb;

import tech.wetech.flexmodel.AbstractExpressionCalculator;
import tech.wetech.flexmodel.ExpressionCalculatorException;
import tech.wetech.flexmodel.jsonlogic.JsonLogic;

import java.util.Map;

/**
 * @author cjbi
 */
public class DefaultMongoExpressionCalculator extends AbstractExpressionCalculator<String> {

  private final JsonLogic jsonLogic = new JsonLogic();

  @Override
  public String calculate(String expression, Map<String, Object> dataMap) throws ExpressionCalculatorException {
    try {
      if (expression == null) {
        throw new ExpressionCalculatorException("Expression is null");
      }
      return jsonLogic.evaluateMongoBsonString(transform(expression));
    } catch (Exception e) {
      throw new ExpressionCalculatorException(e.getMessage(), e);
    }
  }

}
