package tech.wetech.flexmodel.query.expr;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 * 条件表达式
 *
 * @author cjbi
 */
public class Predicate implements Expression {

  private final String fieldName;
  private final String operator;
  private final Object value;

  public Predicate(String fieldName, String operator, Object value) {
    this.fieldName = fieldName;
    this.operator = operator;
    this.value = value;
  }

  @Override
  public Map<String, Object> toMap() {
    if (fieldName == null) {
      return Collections.emptyMap(); // 默认条件返回空 JSON
    }
    return Collections.singletonMap(fieldName, Collections.singletonMap(operator, value));
  }

  public Predicate and(Predicate other) {
    if (this == Expressions.TRUE) {
      return other;
    }
    if (other == Expressions.TRUE) {
      return this;
    }
    return new LogicalPredicate("_and", Arrays.asList(this, other));
  }

  public Predicate or(Predicate other) {
    if (this == Expressions.TRUE || other == Expressions.TRUE) {
      return Expressions.TRUE;
    }
    return new LogicalPredicate("_or", Arrays.asList(this, other));
  }
}
