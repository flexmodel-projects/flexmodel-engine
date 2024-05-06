package tech.wetech.flexmodel.jsonlogic.evaluator.expressions;

import tech.wetech.flexmodel.jsonlogic.ast.JsonLogicArray;
import tech.wetech.flexmodel.jsonlogic.ast.JsonLogicNull;
import tech.wetech.flexmodel.jsonlogic.ast.JsonLogicString;
import tech.wetech.flexmodel.jsonlogic.ast.JsonLogicVariable;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluationException;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluator;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicExpression;

/**
 * @author cjbi
 * @date 2022/11/7
 */
public class TableFieldExpression implements JsonLogicExpression {

  public static final TableFieldExpression INSTANCE = new TableFieldExpression();

  @Override
  public String key() {
    return "table_field";
  }

  @Override
  public <T extends JsonLogicEvaluator> Object evaluate(T evaluator, JsonLogicArray arguments, Object data) throws JsonLogicEvaluationException {
    Object table = evaluator.evaluate(arguments.get(0), data);
    Object field = evaluator.evaluate(arguments.get(1), data);
    return evaluator.evaluate(new JsonLogicVariable(new JsonLogicString(table + "." + field), JsonLogicNull.NULL), data);
  }
}
