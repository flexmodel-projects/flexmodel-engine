package tech.wetech.flexmodel.jsonlogic.ast;

import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.jsonlogic.JsonLogic;
import tech.wetech.flexmodel.jsonlogic.JsonLogicException;
import tech.wetech.flexmodel.jsonlogic.evaluator.sql.NamedPlaceholderHandler;
import tech.wetech.flexmodel.jsonlogic.evaluator.sql.SqlRuntimeContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author cjbi
 * @date 2022/11/6
 */
public class TextTests {

  private static final JsonLogic jsonLogic = new JsonLogic();

  @Test
  void testTableField() throws JsonLogicException {
    String json = """
      {
        "==": [
          { "table_field": ["defaultvaluetest", "wenben1"] },
          { "table_field": ["defaultvaluetest", "wenben1"] }
        ]
      }
      """;
    SqlRuntimeContext sqlRuntimeContext = new SqlRuntimeContext();
    NamedPlaceholderHandler placeholderHandler = new NamedPlaceholderHandler();
    sqlRuntimeContext.setPlaceholderHandler(placeholderHandler);
    String sql = jsonLogic.evaluateSql(json, sqlRuntimeContext);
    assertEquals(" defaultvaluetest.wenben1 = defaultvaluetest.wenben1", sql);
  }

  @Test
  void testContains() throws JsonLogicException {
    String json = """
      {
        "and": [
          {
            "contains": [{ "table_field": ["defaultvaluetest", "wenben1"] }, "二十大"]
          }
        ]
      }
      """;
    SqlRuntimeContext sqlRuntimeContext = new SqlRuntimeContext();
    NamedPlaceholderHandler placeholderHandler = new NamedPlaceholderHandler();
    sqlRuntimeContext.setPlaceholderHandler(placeholderHandler);
    String sql = jsonLogic.evaluateSql(json, sqlRuntimeContext);
    assertEquals(" ( defaultvaluetest.wenben1 like concat('%', concat(:defaultvaluetest_wenben1_0,'%')) and 1=1 )", sql);
    assertTrue(placeholderHandler.getParameters().containsValue("二十大"));
    assertTrue(jsonLogic.evaluateBoolean(json, Map.of("defaultvaluetest", Map.of("wenben1", "党的二十大"))));
  }

  @Test
  void testNotContains() throws JsonLogicException {
    String json = """
      {
        "and": [
          {
            "not_contains": [{ "table_field": ["defaultvaluetest", "wenben1"] }, "二十大"]
          }
        ]
      }
      """;
    SqlRuntimeContext sqlRuntimeContext = new SqlRuntimeContext();
    NamedPlaceholderHandler placeholderHandler = new NamedPlaceholderHandler();
    sqlRuntimeContext.setPlaceholderHandler(placeholderHandler);
    String sql = jsonLogic.evaluateSql(json, sqlRuntimeContext);
    assertEquals(" ( defaultvaluetest.wenben1 not like concat('%', concat(:defaultvaluetest_wenben1_0,'%')) and 1=1 )", sql);
    assertTrue(placeholderHandler.getParameters().containsValue("二十大"));
    assertFalse(jsonLogic.evaluateBoolean(json, Map.of("defaultvaluetest", Map.of("wenben1", "党的二十大"))));
  }

  @Test
  void testIn() throws JsonLogicException {
    String expression1 = """
      {
        "in": [
          {
            "table_field": ["user", "tag"]
          },
          ["A", "B", "C", "D"]
        ]
      }
      """;
    assertTrue(
      jsonLogic.evaluateBoolean(expression1, Map.of("user", Map.of("tag", "B")))
    );

    String expression2 = """
      {
        "not_in": [
          {
            "table_field": ["user", "tag"]
          },
          ["A", "B", "C", "D"]
        ]
      }
      """;
    assertTrue(
      jsonLogic.evaluateBoolean(expression2, Map.of("user", Map.of("tag", "F")))
    );
  }

}
