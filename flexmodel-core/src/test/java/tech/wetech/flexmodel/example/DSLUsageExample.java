package tech.wetech.flexmodel.example;

import tech.wetech.flexmodel.query.Direction;
import tech.wetech.flexmodel.session.Session;

import java.util.List;
import java.util.Map;

import static tech.wetech.flexmodel.query.expr.Expressions.field;

/**
 * DSL使用示例
 * 展示如何使用FlexModel的DSL API进行各种数据库操作
 *
 * @author cjbi
 */
public class DSLUsageExample {

  private final Session session;

  public DSLUsageExample(Session session) {
    this.session = session;
  }

  /**
   * 基本CRUD操作示例
   */
  public void basicCRUDExample() {
    System.out.println("=== 基本CRUD操作示例 ===");

    // 1. 创建（Create）
    System.out.println("\n1. 插入数据");
    User newUser = new User("张三", "zhangsan@example.com", 25);
    int insertResult = session.dsl()
      .insertInto(User.class)
      .values(newUser)
      .execute();
    System.out.println("插入结果: " + insertResult + " 条记录");

    // 2. 读取（Read）
    System.out.println("\n2. 查询数据");
    List<User> users = session.dsl()
      .select()
      .from(User.class)
      .where(field("name").eq("张三"))
      .execute();
    System.out.println("查询到 " + users.size() + " 个用户");
    users.forEach(user -> System.out.println("  - " + user.getName() + " (" + user.getEmail() + ")"));

    // 3. 更新（Update）
    System.out.println("\n3. 更新数据");
    if (!users.isEmpty()) {
      User user = users.get(0);
      int updateResult = session.dsl()
        .update(User.class)
        .set("age", 26)
        .set("status", "updated")
        .whereId(user.getId())
        .execute();
      System.out.println("更新结果: " + updateResult + " 条记录");

      // 验证更新结果
      User updatedUser = session.dsl()
        .select()
        .from(User.class)
        .whereId(user.getId())
        .executeOne();
      System.out.println("更新后年龄: " + updatedUser.getAge());
    }

    // 4. 删除（Delete）
    System.out.println("\n4. 删除数据");
    if (!users.isEmpty()) {
      User user = users.get(0);
      int deleteResult = session.dsl()
        .deleteFrom(User.class)
        .whereId(user.getId())
        .execute();
      System.out.println("删除结果: " + deleteResult + " 条记录");
    }
  }

  /**
   * 复杂查询示例
   */
  public void complexQueryExample() {
    System.out.println("\n=== 复杂查询示例 ===");

    // 1. 条件查询
    System.out.println("\n1. 条件查询");
    List<User> activeUsers = session.dsl()
      .select()
      .from(User.class)
      .where(field("status").eq("active")
        .and(field("age").gte(18))
        .and(field("age").lte(65)))
      .orderBy("age", Direction.DESC)
      .limit(10)
      .execute();
    System.out.println("活跃用户数量: " + activeUsers.size());

    // 2. 聚合查询
    System.out.println("\n2. 聚合查询");
    long totalUsers = session.dsl()
      .select()
      .from(User.class)
      .count();
    System.out.println("总用户数: " + totalUsers);

    long activeUserCount = session.dsl()
      .select()
      .from(User.class)
      .where(field("status").eq("active"))
      .count();
    System.out.println("活跃用户数: " + activeUserCount);

    // 3. 分页查询
    System.out.println("\n3. 分页查询");
    List<User> pagedUsers = session.dsl()
      .select()
      .from(User.class)
      .orderBy("id", Direction.ASC)
      .page(1, 5)
      .execute();
    System.out.println("第一页用户数: " + pagedUsers.size());
  }

  /**
   * 批量操作示例
   */
  public void batchOperationsExample() {
    System.out.println("\n=== 批量操作示例 ===");

    // 1. 批量插入
    System.out.println("\n1. 批量插入");
    for (int i = 1; i <= 5; i++) {
      User user = new User("用户" + i, "user" + i + "@example.com", 20 + i);
      session.dsl()
        .insertInto(User.class)
        .values(user)
        .execute();
    }
    System.out.println("批量插入完成");

    // 2. 批量更新
    System.out.println("\n2. 批量更新");
    int updateResult = session.dsl()
      .update(User.class)
      .set("status", "batch_updated")
      .where(field("name").contains("用户"))
      .execute();
    System.out.println("批量更新结果: " + updateResult + " 条记录");

    // 3. 批量删除
    System.out.println("\n3. 批量删除");
    int deleteResult = session.dsl()
      .deleteFrom(User.class)
      .where(field("name").contains("用户"))
      .execute();
    System.out.println("批量删除结果: " + deleteResult + " 条记录");
  }

  /**
   * 字符串模型名操作示例
   */
  public void stringModelExample() {
    System.out.println("\n=== 字符串模型名操作示例 ===");

    // 1. 使用字符串模型名插入
    System.out.println("\n1. 字符串模型名插入");
    Map<String, Object> userData = Map.of(
      "name", "字符串用户",
      "email", "string@example.com",
      "age", 30,
      "status", "active"
    );
    int insertResult = session.dsl()
      .insertInto("users")
      .values(userData)
      .execute();
    System.out.println("插入结果: " + insertResult + " 条记录");

    // 2. 使用字符串模型名查询
    System.out.println("\n2. 字符串模型名查询");
    List<Map<String, Object>> users = session.dsl()
      .select()
      .from("users")
      .where(field("name").eq("字符串用户"))
      .execute();
    System.out.println("查询到 " + users.size() + " 个用户");
    users.forEach(user -> System.out.println("  - " + user.get("name") + " (" + user.get("email") + ")"));

    // 3. 使用字符串模型名更新
    System.out.println("\n3. 字符串模型名更新");
    if (!users.isEmpty()) {
      Map<String, Object> user = users.get(0);
      int updateResult = session.dsl()
        .update("users")
        .set("age", 31)
        .where(field("id").eq(user.get("id")))
        .execute();
      System.out.println("更新结果: " + updateResult + " 条记录");
    }

    // 4. 使用字符串模型名删除
    System.out.println("\n4. 字符串模型名删除");
    int deleteResult = session.dsl()
      .deleteFrom("users")
      .where(field("name").eq("字符串用户"))
      .execute();
    System.out.println("删除结果: " + deleteResult + " 条记录");
  }

  /**
   * 错误处理示例
   */
  public void errorHandlingExample() {
    System.out.println("\n=== 错误处理示例 ===");

    try {
      // 尝试插入无效数据
      System.out.println("\n1. 尝试插入无效数据");
      session.dsl()
        .insertInto("nonexistent_table")
        .values(Map.of("name", "test"))
        .execute();
    } catch (Exception e) {
      System.out.println("捕获到异常: " + e.getMessage());
    }

    try {
      // 尝试更新不存在的记录
      System.out.println("\n2. 尝试更新不存在的记录");
      int result = session.dsl()
        .update("users")
        .set("name", "test")
        .whereId(999999L)
        .execute();
      System.out.println("更新结果: " + result + " 条记录（0表示没有找到匹配的记录）");
    } catch (Exception e) {
      System.out.println("捕获到异常: " + e.getMessage());
    }

    try {
      // 尝试删除不存在的记录
      System.out.println("\n3. 尝试删除不存在的记录");
      int result = session.dsl()
        .deleteFrom("users")
        .whereId(999999L)
        .execute();
      System.out.println("删除结果: " + result + " 条记录（0表示没有找到匹配的记录）");
    } catch (Exception e) {
      System.out.println("捕获到异常: " + e.getMessage());
    }
  }

  /**
   * 运行所有示例
   */
  public void runAllExamples() {
    System.out.println("开始运行DSL使用示例...");

    try {
      basicCRUDExample();
      complexQueryExample();
      batchOperationsExample();
      stringModelExample();
      errorHandlingExample();

      System.out.println("\n所有示例运行完成！");
    } catch (Exception e) {
      System.err.println("运行示例时发生错误: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
