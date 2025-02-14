package tech.wetech.flexmodel.dsl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author cjbi
 */
// 字段表达式
public class FieldExpression<T> implements Expression {
  private final String fieldName;

  public FieldExpression(String fieldName) {
    this.fieldName = fieldName;
  }

  public Predicate eq(T value) {
    return new Predicate(fieldName, "_eq", value);
  }

  public Predicate ne(T value) {
    return new Predicate(fieldName, "_ne", value);
  }

  public Predicate gt(T value) {
    return new Predicate(fieldName, "_gt", value);
  }

  public Predicate gte(T value) {
    return new Predicate(fieldName, "_gte", value);
  }

  public Predicate lt(T value) {
    return new Predicate(fieldName, "_lt", value);
  }

  public Predicate lte(T value) {
    return new Predicate(fieldName, "_lte", value);
  }

  public Predicate contains(T value) {
    return new Predicate(fieldName, "_contains", value);
  }

  public Predicate notContains(T value) {
    return new Predicate(fieldName, "_not_contains", value);
  }

  public Predicate startsWith(T value) {
    return new Predicate(fieldName, "_starts_with", value);
  }

  public Predicate endsWith(T value) {
    return new Predicate(fieldName, "_end_with", value);
  }

  public Predicate in(Collection<T> values) {
    return new Predicate(fieldName, "_in", values);
  }

  public Predicate nin(Collection<T> values) {
    return new Predicate(fieldName, "_nin", values);
  }

  public Predicate between(T start, T end) {
    return new Predicate(fieldName, "_between", Arrays.asList(start, end));
  }

  @Override
  public Map<String, Object> toMap() {
    return Collections.singletonMap(fieldName, null);
  }
}
