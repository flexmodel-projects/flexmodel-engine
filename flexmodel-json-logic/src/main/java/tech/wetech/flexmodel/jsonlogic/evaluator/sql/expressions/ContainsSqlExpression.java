package tech.wetech.flexmodel.jsonlogic.evaluator.sql.expressions;

import tech.wetech.flexmodel.jsonlogic.ast.JsonLogicArray;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluationException;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluator;
import tech.wetech.flexmodel.jsonlogic.evaluator.sql.PlaceholderHandler;
import tech.wetech.flexmodel.jsonlogic.evaluator.sql.SqlRuntimeContext;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cjbi
 * @date 2022/11/6
 */
public class ContainsSqlExpression implements SqlExpression {

  public static final ContainsSqlExpression CONTAINS = new ContainsSqlExpression(false);

  public static final ContainsSqlExpression NOT_CONTAINS = new ContainsSqlExpression(true);

  private final boolean isNot;

  public ContainsSqlExpression(boolean isNot) {
    this.isNot = isNot;
  }

  @Override
  public String key() {
    return isNot ? "not_contains" : "contains";
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends JsonLogicEvaluator> SqlIdentifier evaluate(T evaluator, JsonLogicArray arguments, Object data) throws JsonLogicEvaluationException {
    SqlRuntimeContext sqlRuntimeContext = (SqlRuntimeContext) data;
    PlaceholderHandler placeholderHandler = sqlRuntimeContext.getPlaceholderHandler();
    Object left = evaluator.evaluate(arguments.get(0), data);
    Object right = evaluator.evaluate(arguments.get(1), data);
    if (right instanceof List<?> list) {
      if (list.isEmpty()) {
        return FALSE;
      }
      if (list.stream().allMatch(i -> i instanceof String || i instanceof Number)) {
        String s = left + " in" + list.stream()
          .map(i -> placeholderHandler.handle(left.toString(), i))
          .collect(Collectors.joining(", ", " (", ") "));
        return new SqlIdentifier(s);
      }
      return new SqlIdentifier(list.stream()
        .map(element -> getSingle(placeholderHandler, left, right, isTableFieldExpression(arguments.get(1))))
        .collect(Collectors.joining(" and ", " (", ") ")));
    }
    return new SqlIdentifier(getSingle(placeholderHandler, left, right, isTableFieldExpression(arguments.get(1))));
  }

  public String getSingle(PlaceholderHandler placeholderHandler, Object left, Object right, boolean rightIsTableField) {
    StringBuilder sb = new StringBuilder(" ");
    sb.append(left);
    sb.append(" ");
    sb.append(isNot ? "not like" : "like");
    sb.append(" ");
    sb.append("concat('%', concat(").append(rightIsTableField ? right : placeholderHandler.handle(left.toString(), right)).append(",'%'))");
    return sb.toString();
  }

}
