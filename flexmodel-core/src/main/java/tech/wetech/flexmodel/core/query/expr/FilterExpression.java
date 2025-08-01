package tech.wetech.flexmodel.core.query.expr;

import java.util.*;

/**
 * @author cjbi
 */
// 字段表达式
public class FilterExpression<T> implements Expression {

  private final String fieldName;

  public FilterExpression(String fieldName) {
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

  public Predicate in(T... values) {
    return new Predicate(fieldName, "_in", List.of(values));
  }

  public Predicate nin(T... values) {
    return new Predicate(fieldName, "_nin", List.of(values));
  }

  public Predicate between(T start, T end) {
    return new Predicate(fieldName, "_between", Arrays.asList(start, end));
  }

  @Override
  public Map<String, Object> toMap() {
    return Collections.singletonMap(fieldName, null);
  }

}
