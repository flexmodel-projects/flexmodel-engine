package tech.wetech.flexmodel.jsonlogic.evaluator;

import tech.wetech.flexmodel.jsonlogic.JsonLogic;
import tech.wetech.flexmodel.jsonlogic.ast.*;
import tech.wetech.flexmodel.jsonlogic.evaluator.expressions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author cjbi
 */
public class PrimitiveTypeJsonLogicEvaluator implements JsonLogicEvaluator {

  protected final List<JsonLogicExpression> expressions = new ArrayList<>();

  public PrimitiveTypeJsonLogicEvaluator() {
    addOperation(TableFieldExpression.INSTANCE);

    addOperation(RadioExpression.INSTANCE);
    addOperation(DatetimeExpression.INSTANCE);
    addOperation(MultipleExpression.INSTANCE);
    addOperation(AttachExpression.INSTANCE);
    addOperation(IdentifierExpression.INSTANCE);
    addOperation(CurrentDatetimeExpression.INSTANCE);
  }

  protected static Object transform(Object value) {
    if (value instanceof Number) {
      return ((Number) value).doubleValue();
    }
    if (value instanceof Map valueMap) {
      String[] dataTypeKeys = {"datetime", "multiple", "radio", "identifier", "attach"};
      for (String dataTypeKey : dataTypeKeys) {
        if (valueMap.containsKey(dataTypeKey)) {
          return valueMap.get(dataTypeKey);
        }
      }
    }
    return value;
  }

  public Object evaluate(JsonLogicPrimitive<?> primitive) {
    switch (primitive.getPrimitiveType()) {
      case NUMBER:
        return ((JsonLogicNumber) primitive).getValue();
      default:
        return primitive.getValue();
    }
  }


  public Object evaluate(JsonLogicVariable variable, Object data) throws JsonLogicEvaluationException {
    Object defaultValue = evaluate(variable.getDefaultValue(), null);

    if (data == null) {
      return defaultValue;
    }

    Object key = evaluate(variable.getKey(), data);

    if (key == null) {
      return Optional.of(data)
        .map(PrimitiveTypeJsonLogicEvaluator::transform)
        .orElse(evaluate(variable.getDefaultValue(), null));
    }

    if (key instanceof Number) {
      int index = ((Number) key).intValue();

      if (JsonLogic.isEligible(data)) {
        List list = (List) data;

        if (index >= 0 && index < list.size()) {
          return transform(list.get(index));
        }
      }

      return defaultValue;
    }

    // Handle the case when the key is a string, potentially referencing an infinitely-deep map: x.y.z
    if (key instanceof String) {
      String name = (String) key;

      if (name.isEmpty()) {
        return data;
      }

      String[] keys = name.split("\\.");
      Object result = data;

      for (String partial : keys) {
        result = evaluatePartialVariable(partial, result);

        if (result == null) {
          return defaultValue;
        }
      }

      return result;
    }

    throw new JsonLogicEvaluationException("var first argument must be null, number, or string");
  }

  private Object evaluatePartialVariable(String key, Object data) throws JsonLogicEvaluationException {
    if (JsonLogic.isEligible(data)) {
      List list = (List) data;
      int index;

      try {
        index = Integer.parseInt(key);
      } catch (NumberFormatException e) {
        throw new JsonLogicEvaluationException(e);
      }

      if (index < 0 || index >= list.size()) {
        return null;
      }

      return transform(list.get(index));
    }

    if (data instanceof Map) {
      return transform(((Map) data).get(key));
    }

    return null;
  }

  public List<Object> evaluate(JsonLogicArray array, Object data) throws JsonLogicEvaluationException {
    List<Object> values = new ArrayList<>(array.size());

    for (JsonLogicNode element : array) {
      values.add(evaluate(element, data));
    }
    return values;
  }

  @Override
  public Object evaluate(JsonLogicOperation operation, Object data) throws JsonLogicEvaluationException {
    JsonLogicExpression expression = getExpression(operation.getOperator());
    return expression.evaluate(this, operation.getArguments(), data);
  }

  @Override
  public Object evaluate(JsonLogicNode node, Object data) throws JsonLogicEvaluationException {
    switch (node.getType()) {
      case PRIMITIVE:
        return evaluate((JsonLogicPrimitive) node);
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
}
