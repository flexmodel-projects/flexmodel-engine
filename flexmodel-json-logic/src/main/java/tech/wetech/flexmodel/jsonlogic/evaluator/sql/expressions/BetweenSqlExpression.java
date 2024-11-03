package tech.wetech.flexmodel.jsonlogic.evaluator.sql.expressions;

import tech.wetech.flexmodel.jsonlogic.ast.JsonLogicArray;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluationException;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluator;
import tech.wetech.flexmodel.jsonlogic.evaluator.sql.SqlRuntimeContext;

import java.util.List;

/**
 * @author cjbi
 */
public class BetweenSqlExpression implements SqlExpression {

  public static final BetweenSqlExpression INSTANCE = new BetweenSqlExpression();

  @Override
  public String key() {
    return "between";
  }

  @Override
  public <T extends JsonLogicEvaluator> SqlIdentifier evaluate(T evaluator, JsonLogicArray arguments, Object data) throws JsonLogicEvaluationException {
    if (arguments.size() == 3) {
      SqlRuntimeContext sqlRuntimeContext = (SqlRuntimeContext) data;
      Object field = evaluator.evaluate(arguments.get(0), data);
      Object left = evaluator.evaluate(arguments.get(1), data);
      Object right = evaluator.evaluate(arguments.get(2), data);
      return new SqlIdentifier(String.format(" %s between %s and %s", field,
        sqlRuntimeContext.getPlaceholderHandler().handle(field.toString(), left),
        sqlRuntimeContext.getPlaceholderHandler().handle(field.toString(), right)
      ));
    } else if (arguments.size() == 2) {
      SqlRuntimeContext sqlRuntimeContext = (SqlRuntimeContext) data;
      Object field = evaluator.evaluate(arguments.get(0), data);
      List<?> arr = (List<?>) evaluator.evaluate(arguments.get(1), data);
      Object left = arr.get(0);
      Object right = arr.get(1);
      return new SqlIdentifier(String.format(" %s between %s and %s", field,
        sqlRuntimeContext.getPlaceholderHandler().handle(field.toString(), left),
        sqlRuntimeContext.getPlaceholderHandler().handle(field.toString(), right)
      ));
    } else {
      throw new JsonLogicEvaluationException("between expression requires 2 or 3 arguments");
    }

  }
}
