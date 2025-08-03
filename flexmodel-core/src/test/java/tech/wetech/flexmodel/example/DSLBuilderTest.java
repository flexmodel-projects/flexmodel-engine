package tech.wetech.flexmodel.example;

import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.session.Session;

import java.util.Map;

import static tech.wetech.flexmodel.query.expr.Expressions.field;

/**
 * DSL构建器测试类
 *
 * @author cjbi
 */
public class DSLBuilderTest {

  private final Session session;

  public DSLBuilderTest(Session session) {
    this.session = session;
  }

  @Test
  public void testDSLInsertBuilder() {
    // 测试字符串模型名插入
    Map<String, Object> record = Map.of("name", "测试用户", "age", 25);
    int rows = session.dsl()
      .insertInto("users")
      .values(record)
      .execute();
    System.out.println("插入记录数: " + rows);

    // 测试实体类插入
    User user = new User("实体用户", "entity@example.com", 30);
    int rows2 = session.dsl()
      .insertInto(User.class)
      .values(user)
      .execute();
    System.out.println("插入实体记录数: " + rows2);
  }

  @Test
  public void testDSLUpdateBuilder() {
    // 测试单个字段更新
    int rows = session.dsl()
      .update("users")
      .set("name", "更新后的名字")
      .where(field("id").eq(1))
      .execute();
    System.out.println("单字段更新记录数: " + rows);

    // 测试多个字段更新
    Map<String, Object> values = Map.of("name", "批量更新", "age", 35);
    int rows2 = session.dsl()
      .update("users")
      .values(values)
      .where(field("id").eq(1))
      .execute();
    System.out.println("多字段更新记录数: " + rows2);

    // 测试实体类更新
    User updateUser = new User("实体更新", "update@example.com", 40);
    int rows3 = session.dsl()
      .update(User.class)
      .values(updateUser)
      .whereId(1L)
      .execute();
    System.out.println("实体类更新记录数: " + rows3);
  }

  @Test
  public void testDSLDeleteBuilder() {
    // 测试按ID删除
    int rows = session.dsl()
      .deleteFrom("users")
      .whereId(1)
      .execute();
    System.out.println("按ID删除记录数: " + rows);

    // 测试按条件删除
    int rows2 = session.dsl()
      .deleteFrom("users")
      .where(field("age").lt(20))
      .execute();
    System.out.println("条件删除记录数: " + rows2);

    // 测试实体类删除
    int rows3 = session.dsl()
      .deleteFrom(User.class)
      .where(field("status").eq("inactive"))
      .execute();
    System.out.println("实体类删除记录数: " + rows3);
  }

  @Test
  public void testDSLQueryBuilder() {
    // 测试基本查询
    var results = session.dsl()
      .select()
      .from("users")
      .where(field("age").gte(18))
      .execute();
    System.out.println("查询结果数量: " + results.size());

    // 测试实体类查询
    var userResults = session.dsl()
      .select()
      .from(User.class)
      .where(field("status").eq("active"))
      .execute();
    System.out.println("用户查询结果数量: " + userResults.size());

    // 测试统计查询
    long count = session.dsl()
      .select()
      .from("users")
      .count();
    System.out.println("总记录数: " + count);
  }

  @Test
  public void testComplexDSLOperations() {
    // 复杂操作示例：插入、更新、查询、删除的完整流程

    // 1. 插入测试数据
    User testUser = new User("复杂测试用户", "complex@example.com", 28);
    int insertRows = session.dsl()
      .insertInto(User.class)
      .values(testUser)
      .execute();
    System.out.println("插入测试数据: " + insertRows + " 条");

    // 2. 查询插入的数据
    var users = session.dsl()
      .select()
      .from(User.class)
      .where(field("email").eq("complex@example.com"))
      .execute();
    System.out.println("查询到用户: " + users.size() + " 个");

    if (!users.isEmpty()) {
      User foundUser = users.get(0);
      System.out.println("找到用户: " + foundUser.getName());

      // 3. 更新用户信息
      int updateRows = session.dsl()
        .update(User.class)
        .set("age", 29)
        .set("status", "updated")
        .whereId(foundUser.getId())
        .execute();
      System.out.println("更新用户: " + updateRows + " 条");

      // 4. 验证更新结果
      User updatedUser = session.dsl()
        .select()
        .from(User.class)
        .whereId(foundUser.getId())
        .executeOne();
      System.out.println("更新后用户年龄: " + updatedUser.getAge());

      // 5. 删除测试数据
      int deleteRows = session.dsl()
        .deleteFrom(User.class)
        .whereId(foundUser.getId())
        .execute();
      System.out.println("删除测试数据: " + deleteRows + " 条");
    }
  }
}
