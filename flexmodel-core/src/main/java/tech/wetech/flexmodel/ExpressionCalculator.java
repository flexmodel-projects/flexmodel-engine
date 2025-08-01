package tech.wetech.flexmodel;

import java.util.Map;

/**
 * @author cjbi
 */
public interface ExpressionCalculator<T> {

  T calculate(String expression, Map<String, Object> dataMap) throws ExpressionCalculatorException;

}
