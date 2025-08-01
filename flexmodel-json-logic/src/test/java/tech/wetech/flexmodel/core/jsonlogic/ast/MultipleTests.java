package tech.wetech.flexmodel.core.jsonlogic.ast;

import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.jsonlogic.JsonLogic;
import tech.wetech.flexmodel.jsonlogic.JsonLogicException;
import tech.wetech.flexmodel.jsonlogic.evaluator.sql.NamedPlaceholderHandler;
import tech.wetech.flexmodel.jsonlogic.evaluator.sql.SqlRuntimeContext;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author cjbi
 * @date 2022/11/6
 */
public class MultipleTests {

  private static final JsonLogic jsonLogic = new JsonLogic();

  @Test
  void testContains() throws JsonLogicException {
    String json = """
      {
        "and": [
          {
            "contains": [
              { "table_field": ["defaultvaluetest", "duoxuan2"] },
              ["天空","大地"]
            ]
          }
        ]
      }
      """;
    SqlRuntimeContext sqlRuntimeContext = new SqlRuntimeContext();
    NamedPlaceholderHandler placeholderHandler = new NamedPlaceholderHandler();
    sqlRuntimeContext.setPlaceholderHandler(placeholderHandler);
    String sql = jsonLogic.evaluateSql(json, sqlRuntimeContext);
    assertEquals(" (defaultvaluetest.duoxuan2 in (:defaultvaluetest_duoxuan2_0, :defaultvaluetest_duoxuan2_1)  and 1=1 )", sql);
    assertTrue(jsonLogic.evaluateBoolean(json, Map.of("defaultvaluetest", Map.of("duoxuan2", Arrays.asList("天空", "大地", "海洋")))));
  }

  @Test
  void testContains2() throws JsonLogicException {
    String json = """
      {
        "and": [
          {
            "contains": [
              { "table_field": ["defaultvaluetest", "duoxuan2"] },
              { "multiple": ["天空", "大地"] }
            ]
          }
        ]
      }
      """;
    SqlRuntimeContext sqlRuntimeContext = new SqlRuntimeContext();
    NamedPlaceholderHandler placeholderHandler = new NamedPlaceholderHandler();
    sqlRuntimeContext.setPlaceholderHandler(placeholderHandler);
    String sql = jsonLogic.evaluateSql(json, sqlRuntimeContext);
    assertEquals(" (defaultvaluetest.duoxuan2 in (:defaultvaluetest_duoxuan2_0, :defaultvaluetest_duoxuan2_1)  and 1=1 )", sql);
    assertTrue(jsonLogic.evaluateBoolean(json, Map.of("defaultvaluetest", Map.of("duoxuan2", Map.of("multiple", Arrays.asList("天空", "大地", "海洋"))))));
  }

  @Test
  void testContains3() throws JsonLogicException {
    String json = """
      {
        "and": [
          {
            "contains": [
              { "table_field": ["defaultvaluetest", "duoxuan2"] },
              "海洋"
            ]
          }
        ]
      }
      """;
    SqlRuntimeContext sqlRuntimeContext = new SqlRuntimeContext();
    NamedPlaceholderHandler placeholderHandler = new NamedPlaceholderHandler();
    sqlRuntimeContext.setPlaceholderHandler(placeholderHandler);
    String sql = jsonLogic.evaluateSql(json, sqlRuntimeContext);
    assertEquals(" ( defaultvaluetest.duoxuan2 like concat('%', concat(:defaultvaluetest_duoxuan2_0,'%')) and 1=1 )", sql);
    assertTrue(jsonLogic.evaluateBoolean(json, Map.of("defaultvaluetest", Map.of("duoxuan2", Arrays.asList("天空", "大地", "海洋")))));
  }

}
