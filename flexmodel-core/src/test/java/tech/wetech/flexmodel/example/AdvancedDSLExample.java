package tech.wetech.flexmodel.example;

import tech.wetech.flexmodel.query.expr.Predicate;
import tech.wetech.flexmodel.session.Session;

import java.util.List;
import java.util.Map;

import static tech.wetech.flexmodel.query.expr.Expressions.field;

/**
 * 高级DSL使用示例
 *
 * @author cjbi
 */
public class AdvancedDSLExample {

  private final Session session;

  public AdvancedDSLExample(Session session) {
    this.session = session;
  }

  /**
   * 基本查询示例
   */
  public void basicQueryExamples() {
    // 1. 简单查询
    List<Map<String, Object>> allUsers = session.dsl()
      .from("users")
      .execute();

    // 2. 带条件的查询
    List<Map<String, Object>> activeUsers = session.dsl()
      .from("users")
      .where(field("status").eq("active"))
      .execute();

    // 3. 带排序的查询
    List<Map<String, Object>> sortedUsers = session.dsl()
      .from("users")
      .orderBy("name", Session.Direction.ASC)
      .execute();

    // 4. 带分页的查询
    List<Map<String, Object>> pagedUsers = session.dsl()
      .from("users")
      .page(1, 10)
      .execute();

    // 5. 限制结果数量
    List<Map<String, Object>> limitedUsers = session.dsl()
      .from("users")
      .limit(5)
      .execute();
  }

  /**
   * 实体类查询示例
   */
  public void entityQueryExamples() {
    // 1. 使用实体类查询
    List<User> users = session.dsl()
      .from(User.class)
      .execute();

    // 2. 使用实体类和方法引用
    List<User> usersByName = session.dsl()
      .from(User.class)
      .where(field(User::getName).eq("张三"))
      .execute();

    // 3. 使用实体类查询单个结果
    User user = session.dsl()
      .from(User.class)
      .where(field(User::getId).eq(1L))
      .executeOne();
  }

  /**
   * 复杂条件查询示例
   */
  public void complexConditionExamples() {
    // 1. AND条件组合
    Predicate andCondition = field(User::getAge).gte(18)
      .and(field(User::getStatus).eq("active"))
      .and(field(User::getName).contains("张"));

    List<User> users = session.dsl()
      .from(User.class)
      .where(andCondition)
      .execute();

    // 2. OR条件组合
    Predicate orCondition = field(User::getStatus).eq("active")
      .or(field(User::getStatus).eq("pending"));

    List<User> activeOrPendingUsers = session.dsl()
      .from(User.class)
      .where(orCondition)
      .execute();

    // 3. 复杂嵌套条件
    Predicate complexCondition = field(User::getAge).gte(18)
      .and(
        field(User::getStatus).eq("active")
          .or(field(User::getStatus).eq("pending"))
      )
      .and(field(User::getName).contains("张"));

    List<User> complexUsers = session.dsl()
      .from(User.class)
      .where(complexCondition)
      .execute();
  }

  /**
   * 字段查询示例
   */
  public void fieldQueryExamples() {
    // 1. 选择特定字段
    List<Map<String, Object>> userNames = session.dsl()
      .select("id", "name", "email")
      .from("users")
      .execute();

    // 2. 使用聚合函数
    List<Map<String, Object>> userStats = session.dsl()
      .select("status", "count(*) as count", "avg(age) as avg_age")
      .from("users")
      .groupBy("status")
      .execute();
  }

  /**
   * 排序示例
   */
  public void sortingExamples() {
    // 1. 单字段排序
    List<User> usersByName = session.dsl()
      .from(User.class)
      .orderBy("name", Session.Direction.ASC)
      .execute();

    // 2. 多字段排序
    List<User> usersByAgeAndName = session.dsl()
      .from(User.class)
      .orderBy("age", Session.Direction.DESC)
      .orderBy("name", Session.Direction.ASC)
      .execute();
  }

  /**
   * 分页示例
   */
  public void paginationExamples() {
    // 1. 基本分页
    List<User> firstPage = session.dsl()
      .from(User.class)
      .page(1, 10)
      .execute();

    // 2. 带排序的分页
    List<User> sortedPage = session.dsl()
      .from(User.class)
      .orderBy("id", Session.Direction.DESC)
      .page(2, 5)
      .execute();

    // 3. 使用limit
    List<User> limitedUsers = session.dsl()
      .from(User.class)
      .limit(5)
      .execute();
  }

  /**
   * 分组示例
   */
  public void groupingExamples() {
    // 1. 基本分组
    List<Map<String, Object>> groupByStatus = session.dsl()
      .select("status", "count(*) as count")
      .from("users")
      .groupBy("status")
      .execute();

    // 2. 多字段分组
    List<Map<String, Object>> groupByStatusAndAge = session.dsl()
      .select("status", "age", "count(*) as count")
      .from("users")
      .groupBy("status", "age")
      .execute();
  }

  /**
   * 连接查询示例
   */
  public void joinExamples() {
    // 1. 内连接
    List<Map<String, Object>> userOrders = session.dsl()
      .select("u.name", "u.email", "o.order_id", "o.amount")
      .from("users")
      .join(joins -> joins
        .addInnerJoin(join -> join
          .setFrom("orders")
          .setAs("o")
          .setLocalField("u.id")
          .setForeignField("o.user_id")
        )
      )
      .where(field("u.status").eq("active"))
      .execute();

    // 2. 左连接
    List<Map<String, Object>> allUsersWithOrders = session.dsl()
      .select("u.name", "u.email", "o.order_id")
      .from("users")
      .leftJoin(joins -> joins
        .addLeftJoin(join -> join
          .setFrom("orders")
          .setAs("o")
          .setLocalField("u.id")
          .setForeignField("o.user_id")
        )
      )
      .execute();

    // 3. 多表连接
    List<Map<String, Object>> complexJoin = session.dsl()
      .select("u.name", "o.order_id", "p.product_name")
      .from("users")
      .join(joins -> joins
        .addInnerJoin(join -> join
          .setFrom("orders")
          .setAs("o")
          .setLocalField("u.id")
          .setForeignField("o.user_id")
        )
        .addInnerJoin(join -> join
          .setFrom("order_items")
          .setAs("oi")
          .setLocalField("o.id")
          .setForeignField("oi.order_id")
        )
        .addInnerJoin(join -> join
          .setFrom("products")
          .setAs("p")
          .setLocalField("oi.product_id")
          .setForeignField("p.id")
        )
      )
      .where(field("u.status").eq("active"))
      .execute();
  }

  /**
   * 统计查询示例
   */
  public void statisticsExamples() {
    // 1. 基本统计
    long totalUsers = session.dsl()
      .from(User.class)
      .count();

    // 2. 条件统计
    long activeUsers = session.dsl()
      .from(User.class)
      .where(field(User::getStatus).eq("active"))
      .count();

    // 3. 检查存在性
    boolean hasActiveUsers = session.dsl()
      .from(User.class)
      .where(field(User::getStatus).eq("active"))
      .exists();

    // 4. 聚合统计
    List<Map<String, Object>> ageStats = session.dsl()
      .select("min(age) as min_age", "max(age) as max_age", "avg(age) as avg_age")
      .from("users")
      .execute();
  }

  /**
   * 字符串条件示例
   */
  public void stringConditionExamples() {
    // 1. 简单字符串条件
    List<User> users = session.dsl()
      .from(User.class)
      .where("age > 18 AND status = 'active'")
      .execute();

    // 2. 复杂字符串条件
    List<User> complexUsers = session.dsl()
      .from(User.class)
      .where("(age >= 18 AND age <= 65) AND (status = 'active' OR status = 'pending')")
      .execute();
  }

  /**
   * 嵌套查询示例
   */
  public void nestedQueryExamples() {
    // 启用嵌套查询
    List<User> usersWithRelations = session.dsl()
      .from(User.class)
      .where(field(User::getStatus).eq("active"))
      .enableNested()
      .execute();
  }

  /**
   * 综合查询示例
   */
  public void comprehensiveExample() {
    // 一个综合的查询示例，展示DSL的强大功能
    List<Map<String, Object>> result = session.dsl()
      .select("u.name", "u.email", "count(o.id) as order_count", "sum(o.amount) as total_amount")
      .from("users")
      .leftJoin(joins -> joins
        .addLeftJoin(join -> join
          .setFrom("orders")
          .setAs("o")
          .setLocalField("u.id")
          .setForeignField("o.user_id")
          .withFilter(field("o.status").eq("completed"))
        )
      )
      .where(field("u.age").gte(18)
        .and(field("u.status").eq("active")))
      .groupBy("u.id", "u.name", "u.email")
      .orderBy("total_amount", Session.Direction.DESC)
      .page(1, 10)
      .execute();

    System.out.println("综合查询结果: " + result);
  }
}
