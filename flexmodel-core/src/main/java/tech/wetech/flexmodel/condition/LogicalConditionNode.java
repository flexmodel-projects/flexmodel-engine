package tech.wetech.flexmodel.condition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 逻辑组合条件节点。
 */
public final class LogicalConditionNode implements ConditionNode {

  private static final LogicalConditionNode EMPTY_AND = new LogicalConditionNode(
    ConditionOperator.AND, Collections.emptyList());

  private final ConditionOperator operator;
  private final List<ConditionNode> children;

  public static LogicalConditionNode emptyAnd() {
    return EMPTY_AND;
  }

  public static LogicalConditionNode of(ConditionOperator operator, List<? extends ConditionNode> children) {
    Objects.requireNonNull(operator, "operator must not be null");
    if (operator != ConditionOperator.AND && operator != ConditionOperator.OR) {
      throw new IllegalArgumentException("LogicalConditionNode only supports AND/OR operators");
    }
    if (children == null || children.isEmpty()) {
      return operator == ConditionOperator.AND ? EMPTY_AND : new LogicalConditionNode(operator, Collections.emptyList());
    }
    List<ConditionNode> filtered = new ArrayList<>();
    for (ConditionNode child : children) {
      if (child != null && !child.isEmpty()) {
        filtered.add(child);
      }
    }
    if (filtered.isEmpty()) {
      return operator == ConditionOperator.AND ? EMPTY_AND : new LogicalConditionNode(operator, Collections.emptyList());
    }
    if (filtered.size() == 1 && filtered.get(0) instanceof LogicalConditionNode single) {
      if (single.getOperator() == operator) {
        return single;
      }
    }
    return new LogicalConditionNode(operator, Collections.unmodifiableList(filtered));
  }

  private LogicalConditionNode(ConditionOperator operator, List<ConditionNode> children) {
    this.operator = operator;
    this.children = children;
  }

  public ConditionOperator getOperator() {
    return operator;
  }

  public List<ConditionNode> getChildren() {
    return children;
  }

  @Override
  public boolean isEmpty() {
    return children.isEmpty();
  }

  @Override
  public String toString() {
    return "LogicalConditionNode{" +
      "operator=" + operator +
      ", children=" + children +
      '}';
  }
}

