package tech.wetech.flexmodel.sql.condition;

import tech.wetech.flexmodel.condition.ConditionNode;
import tech.wetech.flexmodel.condition.ConditionOperator;
import tech.wetech.flexmodel.condition.FieldConditionNode;
import tech.wetech.flexmodel.condition.LogicalConditionNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static tech.wetech.flexmodel.condition.ConditionParser.toCollection;

/**
 * 将条件语法树渲染为 SQL 片段。
 */
public final class SqlConditionRenderer {

  private SqlConditionRenderer() {
  }

  public static String render(ConditionNode node, SqlRenderContext context) {
    if (node == null || node.isEmpty()) {
      return "1=1";
    }
    return renderNode(node, context);
  }

  private static String renderNode(ConditionNode node, SqlRenderContext context) {
    if (node instanceof LogicalConditionNode logical) {
      return renderLogical(logical, context);
    }
    if (node instanceof FieldConditionNode field) {
      return renderField(field, context);
    }
    throw new IllegalArgumentException("Unknown condition node: " + node);
  }

  private static String renderLogical(LogicalConditionNode logical, SqlRenderContext context) {
    List<String> parts = new ArrayList<>();
    for (ConditionNode child : logical.getChildren()) {
      String rendered = renderNode(child, context);
      if (!rendered.isBlank()) {
        parts.add(rendered);
      }
    }
    if (parts.isEmpty()) {
      return logical.getOperator() == ConditionOperator.OR ? "1=0" : "1=1";
    }
    if (parts.size() == 1) {
      return parts.get(0);
    }
    String delimiter = logical.getOperator() == ConditionOperator.AND ? " AND " : " OR ";
    return parts.stream().collect(Collectors.joining(delimiter, "(", ")"));
  }

  private static String renderField(FieldConditionNode field, SqlRenderContext context) {
    String column = context.formatFieldPath(field.getFieldPath());
    PlaceholderHandler placeholderHandler = context.getPlaceholderHandler();
    Object value = field.getValue();

    return switch (field.getOperator()) {
      case EQ -> column + " = " + placeholderHandler.handle(column, value);
      case NE -> column + " <> " + placeholderHandler.handle(column, value);
      case GT -> column + " > " + placeholderHandler.handle(column, value);
      case GTE -> column + " >= " + placeholderHandler.handle(column, value);
      case LT -> column + " < " + placeholderHandler.handle(column, value);
      case LTE -> column + " <= " + placeholderHandler.handle(column, value);
      case IN -> renderIn(column, value, false, placeholderHandler);
      case NIN -> renderIn(column, value, true, placeholderHandler);
      case BETWEEN -> renderBetween(column, value, placeholderHandler);
      case CONTAINS -> renderContains(column, value, false, placeholderHandler);
      case NOT_CONTAINS -> renderContains(column, value, true, placeholderHandler);
      case STARTS_WITH -> renderLike(column, value, "suffix", false, placeholderHandler);
      case ENDS_WITH -> renderLike(column, value, "prefix", false, placeholderHandler);
      default -> throw new IllegalStateException("Unsupported operator: " + field.getOperator());
    };
  }

  private static String renderIn(String column, Object value, boolean negate, PlaceholderHandler placeholderHandler) {
    Collection<?> collection = toCollection(value);
    if (collection.isEmpty()) {
      return negate ? "1=1" : "1=0";
    }
    StringJoiner joiner = new StringJoiner(", ", "(", ")");
    for (Object item : collection) {
      joiner.add(placeholderHandler.handle(column, item));
    }
    return column + (negate ? " NOT IN " : " IN ") + joiner;
  }

  private static String renderBetween(String column, Object value, PlaceholderHandler placeholderHandler) {
    Collection<?> collection = toCollection(value);
    if (collection.size() != 2) {
      throw new IllegalArgumentException("_between operator expects exactly 2 values");
    }
    Object[] values = collection.toArray();
    String start = placeholderHandler.handle(column + "_start", values[0]);
    String end = placeholderHandler.handle(column + "_end", values[1]);
    return column + " BETWEEN " + start + " AND " + end;
  }

  private static String renderContains(String column, Object value, boolean negate, PlaceholderHandler placeholderHandler) {
    Collection<?> collection = value instanceof Collection<?> ? (Collection<?>) value : null;
    if (collection != null) {
      if (collection.isEmpty()) {
        return negate ? "1=1" : "1=0";
      }
      boolean simple = collection.stream().allMatch(item -> item instanceof String || item instanceof Number);
      if (simple) {
        return renderIn(column, collection, negate, placeholderHandler);
      }
      return collection.stream()
        .map(item -> renderLike(column, item, "both", negate, placeholderHandler))
        .collect(Collectors.joining(negate ? " AND " : " OR ", "(", ")"));
    }
    return renderLike(column, value, "both", negate, placeholderHandler);
  }

  private static String renderLike(String column, Object rawValue, String mode, boolean negate, PlaceholderHandler placeholderHandler) {
    if (rawValue == null) {
      return negate ? "1=1" : "1=0";
    }
    String value = rawValue.toString();
    String pattern = switch (mode) {
      case "prefix" -> "%" + value;
      case "suffix" -> value + "%";
      case "both" -> "%" + value + "%";
      default -> value;
    };
    String placeholder = placeholderHandler.handle(column, pattern);
    return column + (negate ? " NOT LIKE " : " LIKE ") + placeholder;
  }
}

