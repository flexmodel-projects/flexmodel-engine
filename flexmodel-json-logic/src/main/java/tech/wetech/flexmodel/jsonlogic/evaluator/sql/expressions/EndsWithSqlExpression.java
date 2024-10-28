package tech.wetech.flexmodel.jsonlogic.evaluator.sql.expressions;

import tech.wetech.flexmodel.jsonlogic.ast.JsonLogicArray;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluationException;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluator;
import tech.wetech.flexmodel.jsonlogic.evaluator.sql.PlaceholderHandler;
import tech.wetech.flexmodel.jsonlogic.evaluator.sql.SqlRuntimeContext;

/**
 * @author cjbi
 * @date 2022/11/6
 */
public class EndsWithSqlExpression implements SqlExpression {

  public static final EndsWithSqlExpression INSTANCE = new EndsWithSqlExpression();

  @Override
  public String key() {
    return "ends_with";
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends JsonLogicEvaluator> SqlIdentifier evaluate(T evaluator, JsonLogicArray arguments, Object data) throws JsonLogicEvaluationException {
    SqlRuntimeContext sqlRuntimeContext = (SqlRuntimeContext) data;
    PlaceholderHandler placeholderHandler = sqlRuntimeContext.getPlaceholderHandler();
    Object left = evaluator.evaluate(arguments.get(0), data);
    Object right = evaluator.evaluate(arguments.get(1), data);
    return new SqlIdentifier(getSingle(placeholderHandler, left, right, isTableFieldExpression(arguments.get(1))));
  }

  public String getSingle(PlaceholderHandler placeholderHandler, Object left, Object right, boolean rightIsTableField) {
    StringBuilder sb = new StringBuilder(" ");
    sb.append(left);
    sb.append(" ");
    sb.append("like");
    sb.append(" ");
    sb.append("concat('%',").append(rightIsTableField ? right : placeholderHandler.handle(left.toString(), right)).append(")");
    return sb.toString();
  }

}
