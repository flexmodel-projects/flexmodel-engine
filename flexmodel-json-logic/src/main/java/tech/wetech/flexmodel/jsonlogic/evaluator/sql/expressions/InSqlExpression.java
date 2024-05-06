package tech.wetech.flexmodel.jsonlogic.evaluator.sql.expressions;

import tech.wetech.flexmodel.jsonlogic.ast.JsonLogicArray;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluationException;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluator;
import tech.wetech.flexmodel.jsonlogic.evaluator.sql.PlaceholderHandler;
import tech.wetech.flexmodel.jsonlogic.evaluator.sql.SqlRuntimeContext;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author cjbi
 */
public class InSqlExpression implements SqlExpression {

  public static final InSqlExpression IN = new InSqlExpression(false);
  public static final InSqlExpression NOT_IN = new InSqlExpression(true);

  private final boolean isNot;

  public InSqlExpression(boolean isNot) {
    this.isNot = isNot;
  }

  @Override
  public String key() {
    return isNot ? "not_in" : "in";
  }

  @Override
  public <T extends JsonLogicEvaluator> SqlIdentifier evaluate(T evaluator, JsonLogicArray arguments, Object data) throws JsonLogicEvaluationException {
    SqlRuntimeContext sqlRuntimeContext = (SqlRuntimeContext) data;
    PlaceholderHandler placeholderHandler = sqlRuntimeContext.getPlaceholderHandler();
    Object left = evaluator.evaluate(arguments.get(0), data);
    Object right = evaluator.evaluate(arguments.get(1), data);
    if (right instanceof Collection<?> collection) {
      if (collection.isEmpty()) {
        return FALSE;
      }
      if (collection.stream().allMatch(i -> i instanceof String || i instanceof Number)) {
        String s = left + (isNot ? " not in" : " in") + collection.stream()
          .map(i -> placeholderHandler.handle(left.toString(), i))
          .collect(Collectors.joining(", ", " (", ") "));
        return new SqlIdentifier(s);
      }
    }
    return FALSE;
  }


}
