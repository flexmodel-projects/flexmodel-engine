package tech.wetech.flexmodel.jsonlogic.ast;

import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.jsonlogic.JsonLogic;
import tech.wetech.flexmodel.jsonlogic.JsonLogicException;
import tech.wetech.flexmodel.jsonlogic.evaluator.sql.NamedPlaceholderHandler;
import tech.wetech.flexmodel.jsonlogic.evaluator.sql.SqlRuntimeContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author cjbi
 * @date 2022/11/6
 */
public class DatetimeTests {

  private static final JsonLogic jsonLogic = new JsonLogic();

  @Test
  void testComparison() throws JsonLogicException {
    String json = """
       {
         "==": [
           { "table_field": ["defaultvaluetest", "riqishijian3"] },
           { "datetime": "2022-11-06T20:06:34.000" }
         ]
       }
      """;
    SqlRuntimeContext sqlRuntimeContext = new SqlRuntimeContext();
    NamedPlaceholderHandler placeholderHandler = new NamedPlaceholderHandler();
    sqlRuntimeContext.setPlaceholderHandler(placeholderHandler);
    String sql = jsonLogic.evaluateSql(json, sqlRuntimeContext);
    sqlRuntimeContext.getPlaceholderHandler().getParameters();
    for (Map.Entry<String, Object> entry : placeholderHandler.getParameters().entrySet()) {
      sql = sql.replace(":" + entry.getKey(), "'" + entry.getValue().toString() + "'");
    }
    assertEquals(" defaultvaluetest.riqishijian3 = '2022-11-06T20:06:34'", sql);
  }

  @Test
  void testRange() throws JsonLogicException {
    String json = """
      {
        "and": [
          {
            ">=": [
              { "table_field": ["testaaaa", "huiyikaishishijian"] },
              { "datetime": "2022-10-01T10:45:12.560" }
            ]
          },
          {
            "<=": [
              { "table_field": ["testaaaa", "huiyijieshushijian"] },
              { "datetime": "2022-10-31T10:45:12.560" }
            ]
          }
        ]
      }
      """;
    SqlRuntimeContext sqlRuntimeContext = new SqlRuntimeContext();
    NamedPlaceholderHandler placeholderHandler = new NamedPlaceholderHandler();
    sqlRuntimeContext.setPlaceholderHandler(placeholderHandler);
    String sql = jsonLogic.evaluateSql(json, sqlRuntimeContext);
    System.out.println(sql);
  }

  @Test
  void testCurrentDatetime() throws JsonLogicException {
    String expression = """
       {
         "and": [
           {
             ">=": [
               { "table_field": ["user", "birthday"] },
               { "table_field": ["user", "birthday2"] }
             ]
           },
           {
             "<=": [
               { "table_field": ["user", "birthday"] },
               { "current_datetime": [] }
             ]
           }
         ]
       }
      """;
    Map<String, Map<String, String>> data = Map.of("user",
      Map.of(
        "birthday", "2000-10-01T10:45:12.560",
        "birthday2", "1999-10-01T10:45:12.560"
      )
    );
    assertTrue(jsonLogic.evaluateBoolean(expression, data));
  }

  @Test
  void testDatetimeOfVar() throws JsonLogicException {
    String expression = """
       {
         "and": [
           {
             ">=": [
               { "var": ["value"] },
               { "datetime": "2000-10-01T10:45:12.560" }
             ]
           },
           {
             "<=": [
               { "var": ["value"] },
               { "current_datetime": [] }
             ]
           }
         ]
       }
      """;
    Map<String, Map<String, String>> data = Map.of("value", Map.of("datetime", "2012-10-01T10:45:12.560"));
    assertTrue(jsonLogic.evaluateBoolean(expression, data));
  }

}
