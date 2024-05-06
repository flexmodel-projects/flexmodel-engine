package tech.wetech.flexmodel.jsonlogic.evaluator.sql.expressions;

import tech.wetech.flexmodel.jsonlogic.ast.JsonLogicArray;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluationException;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluator;

/**
 * @author cjbi
 */
public class CurrentDatetimeSqlExpression implements SqlExpression {

  public static final CurrentDatetimeSqlExpression INSTANCE = new CurrentDatetimeSqlExpression();

  @Override
  public String key() {
    return "current_datetime";
  }

  @Override
  public <T extends JsonLogicEvaluator> SqlIdentifier evaluate(T evaluator, JsonLogicArray arguments, Object data) throws JsonLogicEvaluationException {
    return new SqlIdentifier("now()");
  }
}
