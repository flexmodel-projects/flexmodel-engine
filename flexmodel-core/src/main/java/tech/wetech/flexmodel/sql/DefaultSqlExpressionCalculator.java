package tech.wetech.flexmodel.sql;

import tech.wetech.flexmodel.ExpressionCalculatorException;
import tech.wetech.flexmodel.condition.ConditionNode;
import tech.wetech.flexmodel.sql.condition.InlinePlaceholderHandler;
import tech.wetech.flexmodel.sql.condition.NamedPlaceholderHandler;
import tech.wetech.flexmodel.sql.condition.SqlConditionRenderer;
import tech.wetech.flexmodel.sql.condition.SqlRenderContext;
import tech.wetech.flexmodel.sql.dialect.SqlDialect;

import java.util.Map;

/**
 * @author cjbi
 */
public class DefaultSqlExpressionCalculator extends SqlExpressionCalculator {

  public DefaultSqlExpressionCalculator(SqlDialect sqlDialect) {
    super(sqlDialect);
  }

  @Override
  public String calculateIncludeValue(String expression) throws ExpressionCalculatorException {
    if (expression == null) {
      throw new ExpressionCalculatorException("Expression is null");
    }
    try {
      ConditionNode condition = parseCondition(expression);
      SqlRenderContext context = new SqlRenderContext(sqlDialect.getIdentifierQuoteString(), new InlinePlaceholderHandler());
      return SqlConditionRenderer.render(condition, context);
    } catch (RuntimeException e) {
      throw new ExpressionCalculatorException(e.getMessage(), e);
    }
  }

  @Override
  public SqlClauseResult calculate(String expression, Map<String, Object> dataMap) throws ExpressionCalculatorException {
    if (expression == null) {
      throw new ExpressionCalculatorException("Expression is null");
    }
    try {
      ConditionNode condition = parseCondition(expression);
      NamedPlaceholderHandler placeholderHandler = new NamedPlaceholderHandler();
      SqlRenderContext context = new SqlRenderContext(sqlDialect.getIdentifierQuoteString(), placeholderHandler);
      String sql = SqlConditionRenderer.render(condition, context);
      return new SqlClauseResult(sql, placeholderHandler.getParameters());
    } catch (RuntimeException e) {
      throw new ExpressionCalculatorException(e.getMessage(), e);
    }
  }

}
