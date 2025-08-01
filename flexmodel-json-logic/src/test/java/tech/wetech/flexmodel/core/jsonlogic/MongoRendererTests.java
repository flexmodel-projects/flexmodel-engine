package tech.wetech.flexmodel.core.jsonlogic;

import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.jsonlogic.JsonLogic;
import tech.wetech.flexmodel.jsonlogic.JsonLogicException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author cjbi
 */
public class MongoRendererTests {
  private static final JsonLogic jsonLogic = new JsonLogic();

  @Test
  void test() throws JsonLogicException {
    String json = """
      {
        "and": [
          { ">": [{ "var": "id" }, 2] },
          { "==": ["jack", "name" ] },
          { "<": [{ "var": "age" }, 21] }
        ]
      }
      """;
    String bsonString = jsonLogic.evaluateMongoBsonString(json);
    assertEquals(
      """
        {
         $and: [{ id: { $gt: 2.0 } }
        , { jack: { $eq: "name" } }
        , { age: { $lt: 21.0 } }
        ]
        }
        """,
      bsonString);
  }

  @Test
  void testIn() throws JsonLogicException {
    String expression = """
      {
        "in": [
          {
            "var": "user"
          },
          ["A", "B", "C", 1]
        ]
      }
      """;
    String bsonString = jsonLogic.evaluateMongoBsonString(expression);
    assertEquals(
      """
        { user: { $in: ["A", "B", "C", 1.0] } }
        """,
      bsonString);
  }
}
