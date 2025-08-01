package tech.wetech.flexmodel.sql;

import tech.wetech.flexmodel.AbstractExpressionCalculator;
import tech.wetech.flexmodel.ExpressionCalculatorException;
import tech.wetech.flexmodel.jsonlogic.JsonLogic;
import tech.wetech.flexmodel.sql.dialect.SqlDialect;

/**
 * @author cjbi
 */
public abstract class SqlExpressionCalculator extends AbstractExpressionCalculator<SqlClauseResult> {

  protected final SqlDialect sqlDialect;

  protected static final JsonLogic jsonLogic = new JsonLogic();

  public SqlExpressionCalculator(SqlDialect sqlDialect) {
    this.sqlDialect = sqlDialect;
  }

  public SqlDialect getSqlDialect() {
    return sqlDialect;
  }

  public abstract String calculateIncludeValue(String expression) throws ExpressionCalculatorException;
}
