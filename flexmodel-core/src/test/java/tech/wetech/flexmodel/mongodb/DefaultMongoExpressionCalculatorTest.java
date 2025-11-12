package tech.wetech.flexmodel.mongodb;

import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.ExpressionCalculatorException;
import tech.wetech.flexmodel.JsonUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultMongoExpressionCalculatorTest {

  private final DefaultMongoExpressionCalculator calculator = new DefaultMongoExpressionCalculator();

  @Test
  void shouldRenderSimpleQuery() throws ExpressionCalculatorException {
    String expression = """
      {
        "username": {"_eq": "john"},
        "age": {"_gt": 18}
      }
      """;
    String result = calculator.calculate(expression, Map.of());
    Map<String, Object> document = JsonUtils.parseToMap(result);

    assertEquals(Map.of("$and", List.of(
      Map.of("username", Map.of("$eq", "john")),
      Map.of("age", Map.of("$gt", 18))
    )), document);
  }

  @Test
  void shouldRenderNestedLogicalQuery() throws ExpressionCalculatorException {
    String expression = """
      {
        "_or": [
          {"status": {"_in": ["ACTIVE", "PENDING"]}},
          {
            "_and": [
              {"createdAt": {"_between": ["2023-01-01", "2023-12-31"]}},
              {"name": {"_contains": "张"}}
            ]
          }
        ]
      }
      """;

    String result = calculator.calculate(expression, Map.of());
    Map<String, Object> document = JsonUtils.parseToMap(result);

    assertEquals(Map.of("$or", List.of(
      Map.of("status", Map.of("$in", List.of("ACTIVE", "PENDING"))),
      Map.of("$and", List.of(
        Map.of("createdAt", Map.of("$gte", "2023-01-01", "$lte", "2023-12-31")),
        Map.of("name", Map.of("$regex", ".*张.*"))
      ))
    )), document);
  }

  @Test
  void shouldSupportImplicitEqSyntax() throws ExpressionCalculatorException {
    String expression = """
      {
        "username": "john_doe",
        "age": 20
      }
      """;
    String result = calculator.calculate(expression, Map.of());
    assertEquals("{\"$and\":[{\"username\":{\"$eq\":\"john_doe\"}},{\"age\":{\"$eq\":20}}]}", result);
  }

  @Test
  void shouldRejectNullExpression() {
    assertThrows(ExpressionCalculatorException.class, () -> calculator.calculate(null, Map.of()));
  }
}

