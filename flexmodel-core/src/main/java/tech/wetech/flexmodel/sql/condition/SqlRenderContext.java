package tech.wetech.flexmodel.sql.condition;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * SQL 渲染上下文。
 */
public class SqlRenderContext {

  private final String identifierQuoteString;
  private PlaceholderHandler placeholderHandler;

  public SqlRenderContext(String identifierQuoteString, PlaceholderHandler placeholderHandler) {
    this.identifierQuoteString = identifierQuoteString == null ? "" : identifierQuoteString;
    this.placeholderHandler = placeholderHandler;
  }

  public String quoteIdentifier(String identifier) {
    return identifierQuoteString + identifier + identifierQuoteString;
  }

  public String formatFieldPath(String fieldPath) {
    return Arrays.stream(fieldPath.split("\\."))
      .map(this::quoteIdentifier)
      .collect(Collectors.joining("."));
  }

  public PlaceholderHandler getPlaceholderHandler() {
    return placeholderHandler;
  }

  public void setPlaceholderHandler(PlaceholderHandler placeholderHandler) {
    this.placeholderHandler = placeholderHandler;
  }
}

