package tech.wetech.flexmodel.jsonlogic.evaluator.sql.expressions;

import tech.wetech.flexmodel.jsonlogic.ast.JsonLogicArray;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluationException;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluator;
import tech.wetech.flexmodel.jsonlogic.evaluator.sql.SqlRuntimeContext;

import java.util.Map;

/**
 * @author cjbi
 * @date 2022/9/5
 */
public class ComparisonSqlExpression implements SqlExpression {


  public static final ComparisonSqlExpression EQ = new ComparisonSqlExpression("==");
  public static final ComparisonSqlExpression NE = new ComparisonSqlExpression("!=");
  public static final ComparisonSqlExpression GT = new ComparisonSqlExpression(">");
  public static final ComparisonSqlExpression GTE = new ComparisonSqlExpression(">=");
  public static final ComparisonSqlExpression LT = new ComparisonSqlExpression("<");
  public static final ComparisonSqlExpression LTE = new ComparisonSqlExpression("<=");
  private static final Map<String, Object> OPERATOR_MAP = Map.of("==", "=");
  private final String key;

  private ComparisonSqlExpression(String key) {
    this.key = key;
  }

  @Override
  public String key() {
    return key;
  }

  @Override
  public <T extends JsonLogicEvaluator> SqlIdentifier evaluate(T evaluator, JsonLogicArray arguments, Object data) throws JsonLogicEvaluationException {
    SqlRuntimeContext sqlRuntimeContext = (SqlRuntimeContext) data;
    Object left = evaluator.evaluate(arguments.get(0), data);
    Object right = evaluator.evaluate(arguments.get(1), data);

    StringBuilder sb = new StringBuilder(" ");
    sb.append(handlePlace(sqlRuntimeContext, arguments.get(0), right, left));
    sb.append(" ");
    sb.append(OPERATOR_MAP.getOrDefault(key, key));
    sb.append(" ");
    sb.append(handlePlace(sqlRuntimeContext, arguments.get(1), left, right));
    return new SqlIdentifier(sb.toString());
  }

}
