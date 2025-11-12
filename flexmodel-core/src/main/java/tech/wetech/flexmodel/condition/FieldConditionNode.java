package tech.wetech.flexmodel.condition;

import java.util.Objects;

/**
 * 单字段条件节点。
 */
public final class FieldConditionNode implements ConditionNode {

  private final String fieldPath;
  private final ConditionOperator operator;
  private final Object value;

  public FieldConditionNode(String fieldPath, ConditionOperator operator, Object value) {
    if (fieldPath == null || fieldPath.isBlank()) {
      throw new IllegalArgumentException("fieldPath must not be blank");
    }
    Objects.requireNonNull(operator, "operator must not be null");
    if (operator == ConditionOperator.AND || operator == ConditionOperator.OR) {
      throw new IllegalArgumentException("FieldConditionNode does not support logical operator " + operator);
    }
    this.fieldPath = fieldPath;
    this.operator = operator;
    this.value = value;
  }

  public String getFieldPath() {
    return fieldPath;
  }

  public ConditionOperator getOperator() {
    return operator;
  }

  public Object getValue() {
    return value;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public String toString() {
    return "FieldConditionNode{" +
      "fieldPath='" + fieldPath + '\'' +
      ", operator=" + operator +
      ", value=" + value +
      '}';
  }
}

