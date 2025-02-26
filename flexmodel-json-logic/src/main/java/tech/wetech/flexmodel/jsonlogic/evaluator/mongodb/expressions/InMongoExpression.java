package tech.wetech.flexmodel.jsonlogic.evaluator.mongodb.expressions;

import tech.wetech.flexmodel.jsonlogic.ast.JsonLogicArray;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluationException;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluator;

import java.util.Map;

/**
 * @author cjbi
 * @date 2022/9/6
 */
public class InMongoExpression extends MongoExpression {

  public static final InMongoExpression IN = new InMongoExpression(false);
  public static final InMongoExpression NOT_IN = new InMongoExpression(true);

  private final boolean isNot;

  private InMongoExpression(boolean isNot) {
    this.isNot = isNot;
  }

  @Override
  public String key() {
    return isNot ? "not_in" : "in";
  }

  @Override
  public <T extends JsonLogicEvaluator> String evaluate(T evaluator, JsonLogicArray arguments, Object data) throws JsonLogicEvaluationException {
    if (arguments.size() < 1) {
      throw new JsonLogicEvaluationException("and operator expects at least 1 argument");
    }
    Object left = evaluator.evaluate(arguments.get(0), data);
    Object right = evaluator.evaluate(arguments.get(1), data);
    return simpleRenderTemplate("""
      { <field>: { <operator>: <items> } }
      """, Map.of(
      "field", left,
      "operator", (isNot ? "$nin" : "$in"),
      "items", format(right)
    ));
  }
}
