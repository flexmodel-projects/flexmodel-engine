package tech.wetech.flexmodel.jsonlogic.evaluator.expressions;

import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicEvaluationException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cjbi
 * @date 2022/9/5
 */
public class ComparisonExpression implements PreEvaluatedArgumentsExpression {

  public static final ComparisonExpression GT = new ComparisonExpression(">");
  public static final ComparisonExpression GTE = new ComparisonExpression(">=");
  public static final ComparisonExpression LT = new ComparisonExpression("<");
  public static final ComparisonExpression LTE = new ComparisonExpression("<=");
  private final String key;

  private ComparisonExpression(String key) {
    this.key = key;
  }

  @Override
  public String key() {
    return key;
  }

  @Override
  public Boolean evaluate(List arguments, Object data) throws JsonLogicEvaluationException {
    try {
      List<LocalDateTime> formatArguments = new ArrayList<>();
      if (arguments.get(0) instanceof LocalDateTime localDateTime) {
        formatArguments.add(localDateTime);
      } else if (arguments.get(0) instanceof String startString) {
        formatArguments.add(LocalDateTime.parse(startString, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
      }
      if (arguments.get(1) instanceof LocalDateTime localDateTime) {
        formatArguments.add(localDateTime);
      } else if (arguments.get(1) instanceof String startString) {
        formatArguments.add(LocalDateTime.parse(startString, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
      }
      return evaluateDatetime(formatArguments, data);
    } catch (Exception e) {
      return evaluateNumeric(arguments);
    }
  }

  private Boolean evaluateDatetime(List<LocalDateTime> arguments, Object data) throws JsonLogicEvaluationException {
    int n = arguments.size();
    if (n != 2) {
      throw new JsonLogicEvaluationException("'" + key + "' requires at least 2 arguments");
    }
    LocalDateTime start = arguments.get(0);
    LocalDateTime end = arguments.get(1);
    return switch (key) {
      case "<" -> start.isBefore(end);
      case "<=" -> start.isBefore(end) || start.isEqual(end);
      case ">" -> start.isAfter(end);
      case ">=" -> start.isAfter(end) || start.isEqual(end);
      default -> throw new JsonLogicEvaluationException("'" + key + "' is not a comparison expression");
    };
  }

  private Boolean evaluateNumeric(List arguments) throws JsonLogicEvaluationException {
    int n = arguments.size();
    if (n != 2) {
      throw new JsonLogicEvaluationException("'" + key + "' requires at least 2 arguments");
    }
    double[] values = new double[n];

    for (int i = 0; i < n; i++) {
      Object value = arguments.get(i);

      if (value instanceof String) {
        try {
          values[i] = Double.parseDouble((String) value);
        } catch (NumberFormatException e) {
          return false;
        }
      } else if (!(value instanceof Number)) {
        return false;
      } else {
        values[i] = ((Number) value).doubleValue();
      }
    }
    // Handle regular comparisons
    switch (key) {
      case "<":
        return values[0] < values[1];

      case "<=":
        return values[0] <= values[1];

      case ">":
        return values[0] > values[1];

      case ">=":
        return values[0] >= values[1];

      default:
        throw new JsonLogicEvaluationException("'" + key + "' is not a comparison expression");
    }
  }
}
