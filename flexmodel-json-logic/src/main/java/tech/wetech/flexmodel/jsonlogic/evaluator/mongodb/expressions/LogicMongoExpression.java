package tech.wetech.flexmodel.jsonlogic.evaluator.mongodb.expressions;

import tech.wetech.flexmodel.jsonlogic.ast.JsonLogicArray;
import tech.wetech.flexmodel.jsonlogic.ast.JsonLogicNode;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluationException;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 * @date 2022/9/6
 */
public class LogicMongoExpression extends MongoExpression {

  public static final LogicMongoExpression AND = new LogicMongoExpression(true);
  public static final LogicMongoExpression OR = new LogicMongoExpression(false);

  private final boolean isAnd;

  private LogicMongoExpression(boolean isAnd) {
    this.isAnd = isAnd;
  }

  @Override
  public String key() {
    return isAnd ? "and" : "or";
  }

  @Override
  public <T extends JsonLogicEvaluator> String evaluate(T evaluator, JsonLogicArray arguments, Object data) throws JsonLogicEvaluationException {
    if (arguments.size() < 1) {
      throw new JsonLogicEvaluationException("and operator expects at least 1 argument");
    }
    List<String> items = new ArrayList<>();
    for (JsonLogicNode element : arguments) {
      items.add(evaluator.evaluate(element, data).toString());
    }
    return simpleRenderTemplate("""
     {
      <operator>: <items>
     }
     """, Map.of(
      "operator", (isAnd ? "$and" : "$or"),
      "items", items
    ));
  }
}
