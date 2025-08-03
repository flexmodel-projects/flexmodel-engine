package tech.wetech.flexmodel.query;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试Query类中简化的join方法和Builder模式
 */
public class QueryJoinTest {

  @Test
  public void testSimplifiedInnerJoin() {
    // 使用Builder模式创建带内连接的查询
    Query query = Query.Builder.create()
      .innerJoin(join -> join
        .model("user")
        .as("u")
        .on("id", "user_id")
        .where("status = 'active'")
      )
      .build();

    assertNotNull(query.getJoins());
    assertEquals(1, query.getJoins().getJoins().size());

    Query.Join join = query.getJoins().getJoins().get(0);
    assertEquals("user", join.getFrom());
    assertEquals("u", join.getAs());
    assertEquals("id", join.getLocalField());
    assertEquals("user_id", join.getForeignField());
    assertEquals("status = 'active'", join.getFilter());
    assertEquals(Query.Join.JoinType.INNER_JOIN, join.getJoinType());
  }

  @Test
  public void testSimplifiedLeftJoin() {
    // 使用Builder模式创建带左连接的查询
    Query query = Query.Builder.create()
      .leftJoin(join -> join
        .model("order")
        .as("o")
        .on("user_id", "id")
      )
      .build();

    assertNotNull(query.getJoins());
    assertEquals(1, query.getJoins().getJoins().size());

    Query.Join join = query.getJoins().getJoins().get(0);
    assertEquals("order", join.getFrom());
    assertEquals("o", join.getAs());
    assertEquals("user_id", join.getLocalField());
    assertEquals("id", join.getForeignField());
    assertNull(join.getFilter());
    assertEquals(Query.Join.JoinType.LEFT_JOIN, join.getJoinType());
  }

  @Test
  public void testMultipleJoins() {
    // 使用Builder模式创建多个连接
    Query query = Query.Builder.create()
      .innerJoin(join -> join
        .model("user")
        .as("u")
        .on("id", "user_id")
      )
      .leftJoin(join -> join
        .model("order")
        .as("o")
        .on("user_id", "id")
      )
      .build();

    assertNotNull(query.getJoins());
    assertEquals(2, query.getJoins().getJoins().size());

    Query.Join innerJoin = query.getJoins().getJoins().get(0);
    assertEquals(Query.Join.JoinType.INNER_JOIN, innerJoin.getJoinType());

    Query.Join leftJoin = query.getJoins().getJoins().get(1);
    assertEquals(Query.Join.JoinType.LEFT_JOIN, leftJoin.getJoinType());
  }

  @Test
  public void testJoinBuilderMethods() {
    Query.JoinBuilder builder = new Query.JoinBuilder();

    // 测试所有方法
    builder.model("test_model")
      .as("test_alias")
      .on("local_field", "foreign_field")
      .where("test_filter");

    // 通过反射或其他方式验证内部状态
    // 这里我们主要验证方法调用不会抛出异常
    assertNotNull(builder);
  }

  @Test
  public void testQueryBuilderPattern() {
    // 使用新的Query.Builder模式
    Query query = Query.Builder.create()
      .where("status = 'active'")
      .select("id", "name", "email")
      .innerJoin(join -> join
        .model("user")
        .as("u")
        .on("id", "user_id")
      )
      .leftJoin(join -> join
        .model("order")
        .as("o")
        .on("user_id", "id")
      )
      .groupBy(groupBy -> groupBy.field("user_id"))
      .orderBy(orderBy -> orderBy.asc("name"))
      .page(1, 20)
      .build();

    assertNotNull(query);
    assertEquals("status = 'active'", query.getFilter());
    assertNotNull(query.getProjection());
    assertNotNull(query.getJoins());
    assertEquals(2, query.getJoins().getJoins().size());
    assertNotNull(query.getGroupBy());
    assertNotNull(query.getSort());
    assertNotNull(query.getPage());
  }

  @Test
  public void testQueryBuilderWithComplexSelect() {
    Query query = Query.Builder.create()
      .select(select -> select
        .field("id")
        .field("name", "user_name")
        .count("total_count", "id")
        .sum("total_amount", "amount")
        .avg("avg_score", "score")
      )
      .build();

    assertNotNull(query.getProjection());
    assertEquals(5, query.getProjection().getFields().size());
  }


}
