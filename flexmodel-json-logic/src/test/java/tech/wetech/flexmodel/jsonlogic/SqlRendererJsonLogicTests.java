package tech.wetech.flexmodel.jsonlogic;

import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.jsonlogic.evaluator.sql.IndexPlaceholderHandler;
import tech.wetech.flexmodel.jsonlogic.evaluator.sql.NamedPlaceholderHandler;
import tech.wetech.flexmodel.jsonlogic.evaluator.sql.SqlRuntimeContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author cjbi
 * @date 2022/9/5
 */
public class SqlRendererJsonLogicTests {

  private static final JsonLogic jsonLogic = new JsonLogic();

  @Test
  void testIndex() throws JsonLogicException {
    String json = """
      {
        "and": [
          { ">": [{ "table_field": ["user", "id"] }, 2] },
          { "==": ["jack", { "table_field": ["user", "name"] }] },
          { "<": [{ "table_field": ["user", "age"] }, 21] }
        ]
      }
      """;
    SqlRuntimeContext sqlRuntimeContext = new SqlRuntimeContext();
    IndexPlaceholderHandler placeholderHandler = new IndexPlaceholderHandler();
    sqlRuntimeContext.setPlaceholderHandler(placeholderHandler);
    String sql = jsonLogic.evaluateSql(json, sqlRuntimeContext);
    assertNotNull(sql);
    assertEquals(3, placeholderHandler.getParameters().length);
  }

  @Test
  void testNamed() throws JsonLogicException {
    String json = """
      {
        "or": [
          {
            "and": [
              { ">": [{ "table_field": ["user", "id"] }, 2] },
              { "==": ["jack", { "table_field": ["user", "name"] }] },
              { "<": [{ "table_field": ["user", "age"] }, 21] }
            ]
          },
          {
            "and": [
              { ">": [{ "table_field": ["user", "id"] }, 2] },
              { "==": ["jack", { "table_field": ["user", "name"] }] },
              { "<": [{ "table_field": ["user", "age"] }, 21] }
            ]
          },
          {
            "and": [
              { ">": [{ "var": ["id", 3] }, 2] },
              { "==": ["mark", { "table_field": ["user", "name"] }] },
              { "==": [1, 1] }
            ]
          }
        ]
      }
      """;
    SqlRuntimeContext sqlRuntimeContext = new SqlRuntimeContext();
    NamedPlaceholderHandler placeholderHandler = new NamedPlaceholderHandler();
    sqlRuntimeContext.setPlaceholderHandler(placeholderHandler);
    String sql = jsonLogic.evaluateSql(json, sqlRuntimeContext);
    assertNotNull(sql);
    assertEquals(10, placeholderHandler.getParameters().size());
  }

  @Test
  void testPrimitiveContains() throws JsonLogicException {
    String json = """
      {
        "contains": [
          {
            "table_field": ["xuesheng", "created_by"]
          },
          [2, 3, 4, 5, 6]
        ]
      }
      """;
    SqlRuntimeContext sqlRuntimeContext = new SqlRuntimeContext();
    NamedPlaceholderHandler placeholderHandler = new NamedPlaceholderHandler();
    sqlRuntimeContext.setPlaceholderHandler(placeholderHandler);
    String sql = jsonLogic.evaluateSql(json, sqlRuntimeContext);
    assertEquals(Map.of("xuesheng_created_by_0", 2.0,
      "xuesheng_created_by_1", 3.0,
      "xuesheng_created_by_2", 4.0,
      "xuesheng_created_by_3", 5.0,
      "xuesheng_created_by_4", 6.0), placeholderHandler.getParameters());
    assertEquals("xuesheng.created_by in (:xuesheng_created_by_0, :xuesheng_created_by_1, :xuesheng_created_by_2, :xuesheng_created_by_3, :xuesheng_created_by_4) ",
      sql
    );
  }

  @Test
  void testVar() throws JsonLogicException {
    String expression = """
      { "==": [{ "var": "__flow.status" }, "ACTIVE"] }
      """;
    SqlRuntimeContext sqlRuntimeContext = new SqlRuntimeContext();
    NamedPlaceholderHandler placeholderHandler = new NamedPlaceholderHandler();
    sqlRuntimeContext.setPlaceholderHandler(placeholderHandler);
    sqlRuntimeContext.setIdentifierQuoteString("`");
    String sql = jsonLogic.evaluateSql(expression, sqlRuntimeContext);
    assertEquals(Map.of("__flow_status_0", "ACTIVE"), placeholderHandler.getParameters());
    assertEquals(" `__flow`.`status` = :__flow_status_0", sql);
    String expression2 = """
      { "==": [{ "var": "status" }, "ACTIVE"] }
      """;
    SqlRuntimeContext sqlRuntimeContext2 = new SqlRuntimeContext();
    NamedPlaceholderHandler placeholderHandler2 = new NamedPlaceholderHandler();
    sqlRuntimeContext2.setPlaceholderHandler(placeholderHandler2);
    sqlRuntimeContext2.setIdentifierQuoteString("`");
    String sql2 = jsonLogic.evaluateSql(expression2, sqlRuntimeContext2);
    assertEquals(Map.of("status_0", "ACTIVE"), placeholderHandler2.getParameters());
    assertEquals(" `status` = :status_0", sql2);
  }

  @Test
  void testCurrentDatetime() throws JsonLogicException {
    String expression = """
      { "<=": [{ "table_field": ["user","birthday"] }, {"current_datetime": []}] }
      """;
    SqlRuntimeContext sqlRuntimeContext = new SqlRuntimeContext();
    NamedPlaceholderHandler placeholderHandler = new NamedPlaceholderHandler();
    sqlRuntimeContext.setPlaceholderHandler(placeholderHandler);
    String sql = jsonLogic.evaluateSql(expression, sqlRuntimeContext);
    assertEquals(" user.birthday <= now()",
      sql
    );
  }

  @Test
  void testSqlClause() throws JsonLogicException {
    {
      String expression = """
        { "table_field": [ "sales", "price" ] }
        """;
      SqlRuntimeContext sqlRuntimeContext = new SqlRuntimeContext();
      NamedPlaceholderHandler placeholderHandler = new NamedPlaceholderHandler();
      sqlRuntimeContext.setPlaceholderHandler(placeholderHandler);
      assertEquals("sales.price",
        jsonLogic.evaluateSql(expression, sqlRuntimeContext)
      );
    }
    {
      SqlRuntimeContext sqlRuntimeContext = new SqlRuntimeContext();
      NamedPlaceholderHandler placeholderHandler = new NamedPlaceholderHandler();
      sqlRuntimeContext.setPlaceholderHandler(placeholderHandler);
      String expression2 = """
        { "sum_agg": [ { "table_field": [ "sales", "price" ] } ] }
        """;
      assertEquals("sum(sales.price)",
        jsonLogic.evaluateSql(expression2, sqlRuntimeContext)
      );
    }
    {
      SqlRuntimeContext sqlRuntimeContext = new SqlRuntimeContext();
      NamedPlaceholderHandler placeholderHandler = new NamedPlaceholderHandler();
      sqlRuntimeContext.setPlaceholderHandler(placeholderHandler);
      sqlRuntimeContext.setIdentifierQuoteString("`");
      String expression3 = """
        { "avg_agg": [ { "table_field": [ "sales", "price" ] } ] }
        """;
      assertEquals("avg(`sales`.`price`)",
        jsonLogic.evaluateSql(expression3, sqlRuntimeContext)
      );
    }
    {
      SqlRuntimeContext sqlRuntimeContext = new SqlRuntimeContext();
      NamedPlaceholderHandler placeholderHandler = new NamedPlaceholderHandler();
      sqlRuntimeContext.setPlaceholderHandler(placeholderHandler);
      sqlRuntimeContext.setIdentifierQuoteString("`");
      String expression3 = """
        { "avg_agg": [ { "table_field": [ "sales", "price" ] } ] }
        """;
      assertEquals("avg(`sales`.`price`)",
        jsonLogic.evaluateSql(expression3, sqlRuntimeContext)
      );
    }
    {
      SqlRuntimeContext sqlRuntimeContext = new SqlRuntimeContext();
      NamedPlaceholderHandler placeholderHandler = new NamedPlaceholderHandler();
      sqlRuntimeContext.setPlaceholderHandler(placeholderHandler);
      sqlRuntimeContext.setIdentifierQuoteString("`");
      String expression4 = """
        { "sum_agg": [ { "*": [ { "table_field": [ "sales", "unit_price" ] }, { "table_field": [ "sales", "num" ] } ] } ] }
        """;
      assertEquals("sum(`sales`.`unit_price` * `sales`.`num`)",
        jsonLogic.evaluateSql(expression4, sqlRuntimeContext)
      );
    }
    {
      SqlRuntimeContext sqlRuntimeContext = new SqlRuntimeContext();
      NamedPlaceholderHandler placeholderHandler = new NamedPlaceholderHandler();
      sqlRuntimeContext.setPlaceholderHandler(placeholderHandler);
      String expression5 = """
        { "count_agg": [] }
        """;
      assertEquals("count(*)",
        jsonLogic.evaluateSql(expression5, sqlRuntimeContext)
      );
    }
  }

  @Test
  void testBetween() throws JsonLogicException {
    {
      SqlRuntimeContext sqlRuntimeContext = new SqlRuntimeContext();
      NamedPlaceholderHandler placeholderHandler = new NamedPlaceholderHandler();
      sqlRuntimeContext.setPlaceholderHandler(placeholderHandler);
      sqlRuntimeContext.setIdentifierQuoteString("`");
      String expression0 = """
        {
          "between": [
            { "table_field": ["user", "birthday"] },
            {"datetime": "2023-10-02T00:00:00.000"},
            {"datetime": "2023-11-30T00:00:00.000"}
          ]
         }
        """;
      assertEquals(" `user`.`birthday` between :user_birthday_0 and :user_birthday_1",
        jsonLogic.evaluateSql(expression0, sqlRuntimeContext)
      );
    }
    {
      SqlRuntimeContext sqlRuntimeContext = new SqlRuntimeContext();
      NamedPlaceholderHandler placeholderHandler = new NamedPlaceholderHandler();
      sqlRuntimeContext.setPlaceholderHandler(placeholderHandler);
      sqlRuntimeContext.setIdentifierQuoteString("`");
      String expression1 = """
        {
          "between": [
            { "table_field": ["user", "age"] },
            1,
            15
          ]
         }
        """;
      assertEquals(" `user`.`age` between :user_age_0 and :user_age_1",
        jsonLogic.evaluateSql(expression1, sqlRuntimeContext)
      );
    }
  }

  @Test
  void testIn() throws JsonLogicException {
    {
      SqlRuntimeContext sqlRuntimeContext = new SqlRuntimeContext();
      NamedPlaceholderHandler placeholderHandler = new NamedPlaceholderHandler();
      sqlRuntimeContext.setPlaceholderHandler(placeholderHandler);
      sqlRuntimeContext.setIdentifierQuoteString("`");
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
      assertEquals("`user`.`tag` in (:user_tag_0, :user_tag_1, :user_tag_2, :user_tag_3) ",
        jsonLogic.evaluateSql(expression1, sqlRuntimeContext)
      );
    }
    {
      SqlRuntimeContext sqlRuntimeContext = new SqlRuntimeContext();
      NamedPlaceholderHandler placeholderHandler = new NamedPlaceholderHandler();
      sqlRuntimeContext.setPlaceholderHandler(placeholderHandler);
      sqlRuntimeContext.setIdentifierQuoteString("`");
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
      assertEquals("`user`.`tag` not in (:user_tag_0, :user_tag_1, :user_tag_2, :user_tag_3) ",
        jsonLogic.evaluateSql(expression2, sqlRuntimeContext)
      );
    }
  }

  @Test
  void testContainsWhenIsArray() throws JsonLogicException {
    {
      SqlRuntimeContext sqlRuntimeContext = new SqlRuntimeContext();
      NamedPlaceholderHandler placeholderHandler = new NamedPlaceholderHandler();
      sqlRuntimeContext.setPlaceholderHandler(placeholderHandler);
      sqlRuntimeContext.setIdentifierQuoteString("`");
      String expression = """
        {
          "contains": [
            { "table_field": ["banzujiaoyu", "canjiarenyuan0"] },
            { "table_field": ["renyuanxinxiguanli0", "xingming"] }
          ]
        }
        """;
      assertEquals(" `banzujiaoyu`.`canjiarenyuan0` like concat('%', concat(`renyuanxinxiguanli0`.`xingming`,'%'))",
        jsonLogic.evaluateSql(expression, sqlRuntimeContext)
      );
    }
  }

}
