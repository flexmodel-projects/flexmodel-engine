package tech.wetech.flexmodel.jsonlogic;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author cjbi
 * @date 2022/12/17
 */
public class NumberJsonLogicTests {

  private static final JsonLogic jsonLogic = new JsonLogic();

  @Test
  void testAdd() throws JsonLogicException {
    String json = """
          {"+":[4,2]}
      """;
    //4+2=6
    Number result = jsonLogic.evaluateNumber(json, null);
    assertEquals(6.0, result);
  }

  @Test
  void testMultiAdd() throws JsonLogicException {
    String json = """
          {"+":[2,2,2,2,2]}
      """;
    //2+2+2+2+2=10
    Number result = jsonLogic.evaluateNumber(json, null);
    assertEquals(10.0, result);
  }

  @Test
  void testSingleAdd() throws JsonLogicException {
    String json = "{\"+\":[2,2,2,2,2]}";
    Number result = jsonLogic.evaluateNumber(json, null);
    assertEquals(10.0, result);
  }


  @Test
  void testSubtract() throws JsonLogicException {
    String json = "{\"-\":[4,2]}";
    Number result = jsonLogic.evaluateNumber(json, null);

    assertEquals(2.0, result);
  }

  @Test
  void testSingleSubtract() throws JsonLogicException {
    String json = "{\"-\": 2 }";
    Number result = jsonLogic.evaluateNumber(json, null);

    assertEquals(-2.0, result);
  }

  @Test
  void testMultiply() throws JsonLogicException {
    String json = "{\"*\":[4,2]}";
    Number result = jsonLogic.evaluateNumber(json, null);

    assertEquals(8.0, result);
  }

  @Test
  void testMultiMultiply() throws JsonLogicException {
    String json = "{\"*\":[2,2,2,2,2]}";
    Number result = jsonLogic.evaluateNumber(json, null);

    assertEquals(32.0, result);
  }

  @Test
  void testDivide() throws JsonLogicException {
    String json = "{\"/\":[4,2]}";
    Number result = jsonLogic.evaluateNumber(json, null);

    assertEquals(2.0, result);
  }

  @Test
  void testDivideBy0() throws JsonLogicException {
    String json = "{\"/\":[4,0]}";
    Number result = jsonLogic.evaluateNumber(json, null);

    assertEquals(Double.POSITIVE_INFINITY, result);
  }

  @Test
  void testModulo() throws JsonLogicException {
    String json = "{\"%\": [101,2]}";
    Number result = jsonLogic.evaluateNumber(json, null);

    assertEquals(1.0, result);
  }

//    @Test
//    void testMin() throws JsonLogicException {
//        String json = "{\"min\":[1,2,3]}";
//        Number result = JsonLogic.evaluateNumber(json,null);
//
//        assertEquals(1.0, result);
//    }

//    @Test
//    void testMax() throws JsonLogicException {
//        String json = "{\"max\":[1,2,3]}";
//        Number result = JsonLogic.evaluateNumber(json,null);
//
//        assertEquals(3.0, result);
//    }

  @Test
  void testDivideSingleNumber() throws JsonLogicException {
    assertNull(jsonLogic.evaluateNumber("{\"/\": [0]}", null));
  }

  @Test
  void testVar() throws JsonLogicException {
    String json = """
      {"+":[2,{"var":"a"},{"var":"b"}]}
      """;
    Map<String, String> data = Map.of("a", "5", "b", "3");
    Number result = jsonLogic.evaluateNumber(json, data);
    assertEquals(10.0, result);
  }


}
