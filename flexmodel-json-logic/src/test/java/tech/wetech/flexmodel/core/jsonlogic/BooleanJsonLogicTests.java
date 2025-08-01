package tech.wetech.flexmodel.core.jsonlogic;

import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.jsonlogic.JsonLogic;
import tech.wetech.flexmodel.jsonlogic.JsonLogicException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author cjbi
 * @date 2022/9/4
 */
public class BooleanJsonLogicTests {

  private static final JsonLogic jsonLogic = new JsonLogic();

  @Test
  void testOr() throws JsonLogicException {
    String json = """
      { "or": [{ ">": [1, 2] }, { "<": [1, 2] }] }
      """;
    assertTrue(jsonLogic.evaluateBoolean(json, null));
  }

  @Test
  void testAnd() throws JsonLogicException {
    String json = """
      { "and": [{ ">": [3, 2] }, { "<": [1, 2] }] }
      """;
    assertTrue(jsonLogic.evaluateBoolean(json, null));
  }

  @Test
  void testAndOr() throws JsonLogicException {
    String json = """
      {
        "and": [
          { "and": [{ ">=": [3, 5] }, { "<=": [1, 2] }] },
          { "or": [{ ">": [3, 2] }, { "<": [1, 2] }] }
        ]
      }
      """;
    assertFalse(jsonLogic.evaluateBoolean(json, null));
  }

  @Test
  void testVar() throws JsonLogicException {
    Map<String, Object> data = new HashMap<>();
    data.put("b", 10);
    data.put("c", Collections.singletonMap("cc", 20));
    String json = """
      {
        "and": [
          { ">": [{ "var": ["a", 3] }, 2] },
          { "<": [1, { "var": "b" }] },
          { "<": [{ "var": "c.cc" }, 21] }
        ]
      }
      """;
    assertTrue(jsonLogic.evaluateBoolean(json, data));
  }

  @Test
  void testTableField() throws JsonLogicException {
    Map<String, Object> data = new HashMap<>();
    data.put("name", "jack");
    data.put("age", 20);
    String json = """
          {
            "and": [
              { "==": [{ "table_field": ["user", "name"] }, "jack"] },
              { "<": [18, { "table_field": ["user", "age"] }] },
              { "<": [{ "table_field": ["user", "age"] }, 21] }
            ]
          }
      """;
    assertTrue(jsonLogic.evaluateBoolean(json, Map.of("user", data)));

  }

  @Test
  void testBetween() throws JsonLogicException {
    Map<String, Object> data = new HashMap<>();
    data.put("age", 14);
    String expression = """
      {
        "between": [
          { "table_field": ["user", "age"] },
          1,
          15
        ]
      }
      """;
    assertTrue(jsonLogic.evaluateBoolean(expression, Map.of("user", data)));

    Map<String, Object> data2 = new HashMap<>();
    data2.put("birthday", LocalDateTime.of(2023, 10, 3, 0, 0, 0));
    String expression2 = """
      {
        "between": [
          { "table_field": ["user", "birthday"] },
          {"datetime": "2023-10-02T00:00:00.000"},
          {"datetime": "2023-11-30T00:00:00.000"}
        ]
       }
      """;
    assertTrue(jsonLogic.evaluateBoolean(expression2, Map.of("user", data2)));

  }

}
