package tech.wetech.flexmodel.jsonlogic;

import tech.wetech.flexmodel.jsonlogic.ast.JsonLogicParser;
import tech.wetech.flexmodel.jsonlogic.evaluator.*;
import tech.wetech.flexmodel.jsonlogic.evaluator.mongodb.MongoJsonLogicEvaluator;
import tech.wetech.flexmodel.jsonlogic.evaluator.sql.SqlRenderLogicEvaluator;
import tech.wetech.flexmodel.jsonlogic.evaluator.sql.SqlRuntimeContext;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cjbi
 * @date 2022/9/6
 */
public class JsonLogic {

  private final Map<Class<? extends JsonLogicEvaluator>, JsonLogicEvaluator> evaluators = new HashMap<>();

  public JsonLogic() {
    evaluators.put(StringLogicEvaluator.class, new StringLogicEvaluator());
    evaluators.put(BooleanJsonLogicEvaluator.class, new BooleanJsonLogicEvaluator());
    evaluators.put(NumberJsonLogicEvaluator.class, new NumberJsonLogicEvaluator());
    evaluators.put(SqlRenderLogicEvaluator.class, new SqlRenderLogicEvaluator());
    evaluators.put(MongoJsonLogicEvaluator.class, new MongoJsonLogicEvaluator());
  }

  public static boolean truthy(Object value) {
    if (value == null) {
      return false;
    }

    if (value instanceof Boolean) {
      return (boolean) value;
    }

    if (value instanceof Number) {
      if (value instanceof Double) {
        Double d = (Double) value;

        if (d.isNaN()) {
          return false;
        } else if (d.isInfinite()) {
          return true;
        }
      }

      if (value instanceof Float) {
        Float f = (Float) value;

        if (f.isNaN()) {
          return false;
        } else if (f.isInfinite()) {
          return true;
        }
      }

      return ((Number) value).doubleValue() != 0.0;
    }

    if (value instanceof String) {
      return !((String) value).isEmpty();
    }

    if (value instanceof Collection) {
      return !((Collection) value).isEmpty();
    }

    if (value.getClass().isArray()) {
      return Array.getLength(value) > 0;
    }

    return true;
  }

  public static boolean isEligible(Object data) {
    return data != null && (data instanceof Iterable || data.getClass().isArray());
  }

  public void addBooleanOperation(JsonLogicExpression expression) {
    evaluators.get(BooleanJsonLogicEvaluator.class).addOperation(expression);
  }

  public void addNumberOperation(JsonLogicExpression expression) {
    evaluators.get(NumberJsonLogicEvaluator.class).addOperation(expression);
  }

  public void addSqlOperation(JsonLogicExpression expression) {
    evaluators.get(SqlRenderLogicEvaluator.class).addOperation(expression);
  }

  public void addMongoOperation(JsonLogicExpression expression) {
    evaluators.get(MongoJsonLogicEvaluator.class).addOperation(expression);
  }

  public boolean evaluateBoolean(String json, Object data) throws JsonLogicException {
    return (boolean) evaluators.get(BooleanJsonLogicEvaluator.class).evaluate(JsonLogicParser.parse(json), data);
  }

  public Number evaluateNumber(String json, Object data) throws JsonLogicException {
    return (Number) evaluators.get(NumberJsonLogicEvaluator.class).evaluate(JsonLogicParser.parse(json), data);
  }

  public String evaluateString(String json, Object data) throws JsonLogicException {
    return (String) evaluators.get(StringLogicEvaluator.class).evaluate(JsonLogicParser.parse(json), data);
  }

  public String evaluateSql(String json, SqlRuntimeContext sqlRuntimeContext) throws JsonLogicException {
    return (evaluators.get(SqlRenderLogicEvaluator.class)).evaluate(JsonLogicParser.parse(json), sqlRuntimeContext).toString();
  }

  public String evaluateMongoBsonString(String json) throws JsonLogicException {
    return ((MongoJsonLogicEvaluator) evaluators.get(MongoJsonLogicEvaluator.class)).evaluate(JsonLogicParser.parse(json));
  }

}
