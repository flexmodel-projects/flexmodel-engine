package tech.wetech.flexmodel.core.jsonlogic;

import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.jsonlogic.JsonLogic;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author cjbi
 */
public class StringJsonLogicTests {

  private static final JsonLogic jsonLogic = new JsonLogic();

  @Test
  void testCat() throws Exception {
    String expression = """
      {"cat":["中国经济","航船","行稳致远"]}
      """;
    String str = jsonLogic.evaluateString(expression, Map.of());
    assertEquals("中国经济航船行稳致远", str);
  }

  @Test
  void testSubstr() throws Exception {
    assertEquals(
      "经济航船行稳致远",
      jsonLogic.evaluateString("""
        {"substr":["中国经济航船行稳致远",2]}
        """, Map.of()));

    assertEquals(
      "经济",
      jsonLogic.evaluateString("""
        {"substr":["中国经济航船行稳致远",2,2]}
        """, Map.of()));
  }

  @Test
  void testCaseSensitive() throws Exception {
    assertEquals(
      "ABC",
      jsonLogic.evaluateString("""
        {"upper":["Abc"]}
        """, Map.of()));

    assertEquals(
      "abc",
      jsonLogic.evaluateString("""
        {"lower":["Abc"]}
        """, Map.of()));
  }

}
