package tech.wetech.flexmodel.core.sql;

import tech.wetech.flexmodel.core.AbstractExpressionCalculator;
import tech.wetech.flexmodel.core.ExpressionCalculatorException;
import tech.wetech.flexmodel.core.sql.dialect.SqlDialect;
import tech.wetech.flexmodel.jsonlogic.JsonLogic;

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
