package tech.wetech.flexmodel.jsonlogic.evaluator;

import tech.wetech.flexmodel.jsonlogic.ast.JsonLogicArray;

/**
 * @author cjbi
 * @date 2022/9/5
 */
public interface JsonLogicExpression {
  String key();

  <T extends JsonLogicEvaluator> Object evaluate(T evaluator, JsonLogicArray arguments, Object data) throws JsonLogicEvaluationException;
}
