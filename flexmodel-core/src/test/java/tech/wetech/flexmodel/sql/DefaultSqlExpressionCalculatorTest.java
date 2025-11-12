package tech.wetech.flexmodel.sql;

import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.ExpressionCalculatorException;
import tech.wetech.flexmodel.sql.dialect.MySQLSqlDialect;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultSqlExpressionCalculatorTest {

  private DefaultSqlExpressionCalculator newCalculator() {
    MySQLSqlDialect dialect = new MySQLSqlDialect();
    dialect.setIdentifierQuoteString("`");
    return new DefaultSqlExpressionCalculator(dialect);
  }

  @Test
  void shouldRenderSqlWithNamedParameters() throws ExpressionCalculatorException {
    DefaultSqlExpressionCalculator calculator = newCalculator();
    String filter = """
      {
        "username": {"_eq": "john_doe"},
        "age": {"_gte": 18}
      }
      """;

    SqlClauseResult result = calculator.calculate(filter, Map.of());

    assertEquals("(`username` = :username_0 AND `age` >= :age_1)", result.sqlClause());
    assertEquals("john_doe", result.args().get("username_0"));
    assertEquals(18, result.args().get("age_1"));
  }

  @Test
  void shouldRenderSqlWithNestedLogicalOperators() throws ExpressionCalculatorException {
    DefaultSqlExpressionCalculator calculator = newCalculator();
    String filter = """
      {
        "_or": [
          {"status": {"_eq": "ACTIVE"}},
          {"status": {"_eq": "PENDING"}},
          {
            "_and": [
              {"age": {"_between": [18, 30]}},
              {"name": {"_contains": "张"}}
            ]
          }
        ]
      }
      """;

    SqlClauseResult result = calculator.calculate(filter, Map.of());

    assertEquals("(`status` = :status_0 OR `status` = :status_1 OR (`age` BETWEEN :age_start_2 AND :age_end_3 AND `name` LIKE :name_4))", result.sqlClause());
    assertEquals(List.of("ACTIVE", "PENDING"), List.of(result.args().get("status_0"), result.args().get("status_1")));
    assertEquals(18, result.args().get("age_start_2"));
    assertEquals(30, result.args().get("age_end_3"));
    assertEquals("%张%", result.args().get("name_4"));
  }

  @Test
  void shouldSupportImplicitEqSyntax() throws ExpressionCalculatorException {
    DefaultSqlExpressionCalculator calculator = newCalculator();
    String filter = """
      {
        "username": "john_doe",
        "age": 20
      }
      """;

    SqlClauseResult result = calculator.calculate(filter, Map.of());

    assertEquals("(`username` = :username_0 AND `age` = :age_1)", result.sqlClause());
    assertEquals("john_doe", result.args().get("username_0"));
    assertEquals(20, result.args().get("age_1"));
  }

  @Test
  void shouldInlineValuesForIncludeValue() throws ExpressionCalculatorException {
    DefaultSqlExpressionCalculator calculator = newCalculator();
    String filter = """
      {
        "price": {"_between": [100, 200]},
        "remark": {"_not_contains": "测试"}
      }
      """;

    String inline = calculator.calculateIncludeValue(filter);
    assertEquals("(`price` BETWEEN 100 AND 200 AND `remark` NOT LIKE '%测试%')", inline);
  }

  @Test
  void shouldRejectNullExpression() {
    DefaultSqlExpressionCalculator calculator = newCalculator();
    assertThrows(ExpressionCalculatorException.class, () -> calculator.calculate(null, Map.of()));
    assertThrows(ExpressionCalculatorException.class, () -> calculator.calculateIncludeValue(null));
  }
}

