package tech.wetech.flexmodel.jsonlogic.evaluator.expressions;

import tech.wetech.flexmodel.jsonlogic.ast.JsonLogicArray;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluationException;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluator;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicExpression;

import java.util.List;

/**
 * @author cjbi
 * @date 2022/9/5
 */
public interface PreEvaluatedArgumentsExpression extends JsonLogicExpression {

  Object evaluate(List arguments, Object data) throws JsonLogicEvaluationException;

  @Override

  default Object evaluate(JsonLogicEvaluator evaluator, JsonLogicArray arguments, Object data)
    throws JsonLogicEvaluationException {
    return evaluate(evaluator.evaluate(arguments, data), data);
  }

}
