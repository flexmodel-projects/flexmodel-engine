package tech.wetech.flexmodel.jsonlogic.evaluator.mongodb.expressions;

import tech.wetech.flexmodel.jsonlogic.ast.JsonLogicArray;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluationException;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluator;

import java.time.LocalTime;
import java.util.Map;
import java.util.Objects;


/**
 * @author cjbi
 */
public class ComparisonMongoExpression extends MongoExpression {


  public static final ComparisonMongoExpression EQ = new ComparisonMongoExpression("==", "$eq");
  public static final ComparisonMongoExpression NE = new ComparisonMongoExpression("!=", "$ne");
  public static final ComparisonMongoExpression GT = new ComparisonMongoExpression(">", "$gt");
  public static final ComparisonMongoExpression GTE = new ComparisonMongoExpression(">=", "$gte");
  public static final ComparisonMongoExpression LT = new ComparisonMongoExpression("<", "$lt");
  public static final ComparisonMongoExpression LTE = new ComparisonMongoExpression("<=", "$lte");
  private final String key;
  private final String operator;

  public ComparisonMongoExpression(String key, String operator) {
    this.key = key;
    this.operator = operator;
  }


  @Override
  public String key() {
    return key;
  }

  // { <field>: { $eq: <value> } }
  @Override
  public <T extends JsonLogicEvaluator> Object evaluate(T evaluator, JsonLogicArray arguments, Object data) throws JsonLogicEvaluationException {
    String left = Objects.toString(evaluator.evaluate(arguments.get(0), data));
    Object right = evaluator.evaluate(arguments.get(1), data);
    if(left.contains(".")){
      left = left.split("\\.")[1];
    }
    return simpleRenderTemplate("""
      { <field>: { <operator>: <value> } }
      """, Map.of(
      "field", left,
      "operator", operator,
      "value", format(right)
    ));
  }

  public static void main(String[] args) {
    System.out.println(ComparisonMongoExpression.EQ.simpleRenderTemplate("""
      { <field>: { <operator>: <value> } }
      """, Map.of(
      "field", "aaa",
      "operator", "$eq",
      "value", ComparisonMongoExpression.EQ.format(LocalTime.now())
    )));
  }

}
