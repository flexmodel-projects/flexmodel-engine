package tech.wetech.flexmodel.jsonlogic;

import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.jsonlogic.evaluator.sql.IndexPlaceholderHandler;
import tech.wetech.flexmodel.jsonlogic.evaluator.sql.NamedPlaceholderHandler;
import tech.wetech.flexmodel.jsonlogic.evaluator.sql.SqlRuntimeContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author cjbi
 */
public class SqlRendererIdentifierQuoteTests {

  private static final JsonLogic jsonLogic = new JsonLogic();

  @Test
  void testIndexSql() throws Exception {
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
    sqlRuntimeContext.setIdentifierQuoteString("`");
    IndexPlaceholderHandler placeholderHandler = new IndexPlaceholderHandler();
    sqlRuntimeContext.setPlaceholderHandler(placeholderHandler);
    String sql = jsonLogic.evaluateSql(json, sqlRuntimeContext);
    assertEquals(
      " ( `user`.`id` > ? and  ? = `user`.`name` and  `user`.`age` < ? )",
      sql);
    assertEquals(3, placeholderHandler.getParameters().length);
  }

  @Test
  void testNamedSql() throws Exception {
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
    sqlRuntimeContext.setIdentifierQuoteString("\"");
    NamedPlaceholderHandler placeholderHandler = new NamedPlaceholderHandler();
    sqlRuntimeContext.setPlaceholderHandler(placeholderHandler);
    String sql = jsonLogic.evaluateSql(json, sqlRuntimeContext);
    assertEquals(Map.of("xuesheng_created_by_0", 2.0,
      "xuesheng_created_by_1", 3.0,
      "xuesheng_created_by_2", 4.0,
      "xuesheng_created_by_3", 5.0,
      "xuesheng_created_by_4", 6.0), placeholderHandler.getParameters());
    assertEquals("\"xuesheng\".\"created_by\" in (:xuesheng_created_by_0, :xuesheng_created_by_1, :xuesheng_created_by_2, :xuesheng_created_by_3, :xuesheng_created_by_4) ",
      sql
    );
  }

}
