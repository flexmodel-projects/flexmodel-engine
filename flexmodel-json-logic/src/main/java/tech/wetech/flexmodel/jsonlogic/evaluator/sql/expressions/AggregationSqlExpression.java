package tech.wetech.flexmodel.jsonlogic.evaluator.sql.expressions;

import tech.wetech.flexmodel.jsonlogic.ast.JsonLogicArray;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluationException;
import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluator;

import java.util.function.Function;

/**
 * @author cjbi
 */
public class AggregationSqlExpression implements SqlExpression {

  public static final AggregationSqlExpression COUNT = new AggregationSqlExpression("count_agg", i -> new SqlIdentifier("count(" + i + ")"));
  public static final AggregationSqlExpression MAX = new AggregationSqlExpression("max_agg", i -> new SqlIdentifier("max(" + i + ")"));
  public static final AggregationSqlExpression MIN = new AggregationSqlExpression("min_agg", i -> new SqlIdentifier("min(" + i + ")"));
  public static final AggregationSqlExpression SUM = new AggregationSqlExpression("sum_agg", i -> new SqlIdentifier("sum(" + i + ")"));
  public static final AggregationSqlExpression AVG = new AggregationSqlExpression("avg_agg", i -> new SqlIdentifier("avg(" + i + ")"));

  private final String key;
  private final Function<String, SqlIdentifier> fn;

  public AggregationSqlExpression(String key, Function<String, SqlIdentifier> fn) {
    this.key = key;
    this.fn = fn;
  }

  @Override
  public String key() {
    return key;
  }

  @Override
  public <T extends JsonLogicEvaluator> SqlIdentifier evaluate(T evaluator, JsonLogicArray arguments, Object data) throws JsonLogicEvaluationException {
    if (key.equals("count_agg") && arguments.isEmpty()) {
      return fn.apply("*");
    }
    return fn.apply(evaluator.evaluate(arguments.get(0), data).toString());
  }
}
