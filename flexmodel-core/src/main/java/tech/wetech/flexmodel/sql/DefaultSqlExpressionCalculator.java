package tech.wetech.flexmodel.sql;

import tech.wetech.flexmodel.ExpressionCalculatorException;
import tech.wetech.flexmodel.jsonlogic.evaluator.sql.NamedPlaceholderHandler;
import tech.wetech.flexmodel.jsonlogic.evaluator.sql.SqlRuntimeContext;
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
    try {
      if (expression == null) {
        throw new ExpressionCalculatorException("Expression is null");
      }
      SqlRuntimeContext sqlRuntimeContext = new SqlRuntimeContext();
      sqlRuntimeContext.setIdentifierQuoteString(sqlDialect.getIdentifierQuoteString());
      return jsonLogic.evaluateSql(transform(expression), sqlRuntimeContext);
    } catch (Exception e) {
      throw new ExpressionCalculatorException(e.getMessage(), e);
    }
  }

  @Override
  public SqlClauseResult calculate(String expression, Map<String, Object> dataMap) throws ExpressionCalculatorException {
    try {
      if (expression == null) {
        throw new ExpressionCalculatorException("Expression is null");
      }
      SqlRuntimeContext sqlRuntimeContext = new SqlRuntimeContext();
      sqlRuntimeContext.setIdentifierQuoteString(sqlDialect.getIdentifierQuoteString());
      NamedPlaceholderHandler placeholderHandler = new NamedPlaceholderHandler();
      sqlRuntimeContext.setPlaceholderHandler(placeholderHandler);
      String sql = jsonLogic.evaluateSql(transform(expression), sqlRuntimeContext);
      return new SqlClauseResult(sql, placeholderHandler.getParameters());
    } catch (Exception e) {
      throw new ExpressionCalculatorException(e.getMessage(), e);
    }
  }

}
