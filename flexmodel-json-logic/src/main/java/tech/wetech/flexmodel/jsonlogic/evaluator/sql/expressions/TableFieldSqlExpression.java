package tech.wetech.flexmodel.jsonlogic.evaluator.sql.expressions;

import tech.wetech.flexmodel.jsonlogic.ast.JsonLogicArray;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluationException;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluator;
import tech.wetech.flexmodel.jsonlogic.evaluator.sql.SqlRuntimeContext;

/**
 * @author cjbi
 * @date 2022/11/7
 */
public class TableFieldSqlExpression implements SqlExpression {

  public static final TableFieldSqlExpression INSTANCE = new TableFieldSqlExpression();

  @Override
  public String key() {
    return "table_field";
  }

  @Override
  public <T extends JsonLogicEvaluator> SqlIdentifier evaluate(T evaluator, JsonLogicArray arguments, Object data) throws JsonLogicEvaluationException {
    SqlRuntimeContext sqlRuntimeContext = (SqlRuntimeContext) data;
    String table = (String) evaluator.evaluate(arguments.get(0), data);
    String field = (String) evaluator.evaluate(arguments.get(1), data);
    String value = sqlRuntimeContext.quoteIdentifier(table) + "." + sqlRuntimeContext.quoteIdentifier(field);
    return new SqlTableField(table, field, value);
  }
}
