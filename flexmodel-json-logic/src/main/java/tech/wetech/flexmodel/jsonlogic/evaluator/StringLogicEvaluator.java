package tech.wetech.flexmodel.jsonlogic.evaluator;

import tech.wetech.flexmodel.jsonlogic.evaluator.expressions.CaseSensitiveExpression;
import tech.wetech.flexmodel.jsonlogic.evaluator.expressions.ConcatenateExpression;
import tech.wetech.flexmodel.jsonlogic.evaluator.expressions.SubstringExpression;

/**
 * @author cjbi
 */
public class StringLogicEvaluator extends PrimitiveTypeJsonLogicEvaluator implements JsonLogicEvaluator {

  public StringLogicEvaluator() {
    expressions.add(ConcatenateExpression.INSTANCE);
    expressions.add(SubstringExpression.INSTANCE);
    expressions.add(CaseSensitiveExpression.LOWER);
    expressions.add(CaseSensitiveExpression.UPPER);
  }

}
