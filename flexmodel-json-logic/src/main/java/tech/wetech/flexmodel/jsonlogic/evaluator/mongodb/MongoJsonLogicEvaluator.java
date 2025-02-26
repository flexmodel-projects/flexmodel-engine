package tech.wetech.flexmodel.jsonlogic.evaluator.mongodb;

import tech.wetech.flexmodel.jsonlogic.ast.*;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluationException;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluator;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicExpression;
import tech.wetech.flexmodel.jsonlogic.evaluator.mongodb.expressions.ComparisonMongoExpression;
import tech.wetech.flexmodel.jsonlogic.evaluator.mongodb.expressions.InMongoExpression;
import tech.wetech.flexmodel.jsonlogic.evaluator.mongodb.expressions.LogicMongoExpression;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cjbi
 */
public class MongoJsonLogicEvaluator implements JsonLogicEvaluator {

  protected final List<JsonLogicExpression> expressions = new ArrayList<>();

  public MongoJsonLogicEvaluator() {
    addOperation(LogicMongoExpression.AND);
    addOperation(LogicMongoExpression.OR);

    addOperation(ComparisonMongoExpression.EQ);
    addOperation(ComparisonMongoExpression.NE);
    addOperation(ComparisonMongoExpression.GT);
    addOperation(ComparisonMongoExpression.GTE);
    addOperation(ComparisonMongoExpression.LT);
    addOperation(ComparisonMongoExpression.LTE);

    addOperation(InMongoExpression.IN);
    addOperation(InMongoExpression.NOT_IN);
  }

  @Override
  public Object evaluate(JsonLogicOperation operation, Object data) throws JsonLogicEvaluationException {
    JsonLogicExpression expression = getExpression(operation.getOperator());
    return expression.evaluate(this, operation.getArguments(), data);
  }

  @Override
  public List<?> evaluate(JsonLogicArray array, Object data) throws JsonLogicEvaluationException {
    List<Object> values = new ArrayList<>(array.size());

    for (JsonLogicNode element : array) {
      values.add(evaluate(element, data));
    }
    return values;
  }

  public Object evaluate(JsonLogicPrimitive<?> primitive, Object data) {
    switch (primitive.getPrimitiveType()) {
      case NUMBER:
        return ((JsonLogicNumber) primitive).getValue();
      default:
        return primitive.getValue();
    }
  }

  public Object evaluate(JsonLogicVariable variable, Object data) {
    return evaluate((JsonLogicPrimitive) variable.getKey(), data);
  }

  @Override
  public Object evaluate(JsonLogicNode node, Object data) throws JsonLogicEvaluationException {
    switch (node.getType()) {
      case PRIMITIVE:
        return evaluate((JsonLogicPrimitive) node, data);
      case VARIABLE:
        return evaluate((JsonLogicVariable) node, data);
      case ARRAY:
        return evaluate((JsonLogicArray) node, data);
      default:
        return evaluate((JsonLogicOperation) node, data);
    }
  }


  @Override
  public List<JsonLogicExpression> getExpressions() {
    return expressions;
  }

  public String evaluate(JsonLogicNode node) throws JsonLogicEvaluationException {
    return evaluate(node, null).toString();
  }
}
