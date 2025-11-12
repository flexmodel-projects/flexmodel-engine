package tech.wetech.flexmodel;

import tech.wetech.flexmodel.condition.ConditionNode;
import tech.wetech.flexmodel.condition.ConditionParser;

/**
 * 条件计算器抽象基类，提供条件解析能力。
 *
 * @author cjbi
 */
public abstract class AbstractExpressionCalculator<T> implements ExpressionCalculator<T> {

  private static final ConditionParser CONDITION_PARSER = new ConditionParser();

  protected ConditionNode parseCondition(String expression) {
    return CONDITION_PARSER.parse(expression);
  }
}
