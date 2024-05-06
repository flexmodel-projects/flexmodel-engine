package tech.wetech.flexmodel.jsonlogic.evaluator.expressions;

import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluationException;

import java.util.List;

/**
 * @author cjbi
 */
public class SubstringExpression implements PreEvaluatedArgumentsExpression {

  public static final SubstringExpression INSTANCE = new SubstringExpression();

  private SubstringExpression() {
    // Use INSTANCE instead.
  }

  @Override
  public String key() {
    return "substr";
  }

  @Override
  public Object evaluate(List arguments, Object data) throws JsonLogicEvaluationException {
    if (arguments.size() < 2 || arguments.size() > 3) {
      throw new JsonLogicEvaluationException("substr expects 2 or 3 arguments");
    }

    if (!(arguments.get(1) instanceof Double)) {
      throw new JsonLogicEvaluationException("first argument to substr must be a number");
    }

    String value = arguments.get(0).toString();
    int startIndex;
    int endIndex;

    if (arguments.size() == 2) {
      startIndex = ((Double) arguments.get(1)).intValue();
      endIndex = value.length();

      if (startIndex < 0) {
        startIndex = endIndex + startIndex;
      }

      if (startIndex < 0) {
        return "";
      }
    }
    else {
      if (!(arguments.get(2) instanceof Double)) {
        throw new JsonLogicEvaluationException("second argument to substr must be an integer");
      }

      startIndex = ((Double) arguments.get(1)).intValue();

      if (startIndex < 0) {
        startIndex = value.length() + startIndex;
      }

      endIndex = ((Double) arguments.get(2)).intValue();

      if (endIndex < 0) {
        endIndex = value.length() + endIndex;
      }
      else {
        endIndex += startIndex;
      }

      if (startIndex > endIndex || endIndex > value.length()) {
        return "";
      }
    }

    return value.substring(startIndex, endIndex);
  }
}
