package tech.wetech.flexmodel.jsonlogic.evaluator;

import tech.wetech.flexmodel.jsonlogic.ast.JsonLogicArray;
import tech.wetech.flexmodel.jsonlogic.ast.JsonLogicNode;
import tech.wetech.flexmodel.jsonlogic.ast.JsonLogicOperation;

import java.util.List;

/**
 * @author cjbi
 * @date 2022/9/4
 */
public interface JsonLogicEvaluator {

  Object evaluate(JsonLogicOperation operation, Object data) throws JsonLogicEvaluationException;

  List<?> evaluate(JsonLogicArray array, Object data) throws JsonLogicEvaluationException;

  Object evaluate(JsonLogicNode node, Object data) throws JsonLogicEvaluationException;

  default JsonLogicEvaluator addOperation(JsonLogicExpression expression) {
    getExpressions().add(expression);
    return this;
  }

  default JsonLogicExpression getExpression(String key) throws JsonLogicEvaluationException {
    return getExpressions().stream()
      .filter(e -> e.key().equals(key))
      .findFirst()
      .orElseThrow(() -> new JsonLogicEvaluationException("Undefined operation '" + key + "'"));
  }

  List<JsonLogicExpression> getExpressions();

}
