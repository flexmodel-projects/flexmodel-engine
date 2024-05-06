package tech.wetech.flexmodel.jsonlogic.evaluator.expressions;

import tech.wetech.flexmodel.jsonlogic.ast.JsonLogicArray;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluationException;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluator;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicExpression;

/**
 * @author cjbi
 * @date 2022/11/7
 */
public class AttachExpression implements JsonLogicExpression {

  public static final AttachExpression INSTANCE = new AttachExpression();

  @Override
  public String key() {
    return "attach";
  }

  @Override
  public <T extends JsonLogicEvaluator> Object evaluate(T evaluator, JsonLogicArray arguments, Object data) throws JsonLogicEvaluationException {
    return evaluator.evaluate(arguments, data);
  }

}
