package tech.wetech.flexmodel.jsonlogic.evaluator.expressions;

import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluationException;

import java.util.List;

/**
 * @author cjbi
 */
public class CaseSensitiveExpression implements PreEvaluatedArgumentsExpression {

  public static final CaseSensitiveExpression UPPER = new CaseSensitiveExpression("upper");
  public static final CaseSensitiveExpression LOWER = new CaseSensitiveExpression("lower");

  private final String key;

  public CaseSensitiveExpression(String key) {
    this.key = key;
  }

  @Override
  public String key() {
    return key;
  }

  @Override
  public Object evaluate(List arguments, Object data) throws JsonLogicEvaluationException {
    if (arguments.size() != 1) {
      throw new JsonLogicEvaluationException("equality expressions expect exactly 1 arguments");
    }
    String str = (String) arguments.get(0);
    if (key.equals("upper")) {
      return str.toUpperCase();
    }
    if (key.equals("lower")) {
      return str.toLowerCase();
    }
    return str;
  }
}
