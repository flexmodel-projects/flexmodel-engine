package tech.wetech.flexmodel.example;

import tech.wetech.flexmodel.query.Direction;
import tech.wetech.flexmodel.query.expr.Expressions;
import tech.wetech.flexmodel.query.expr.Predicate;
import tech.wetech.flexmodel.session.Session;

import java.util.List;
import java.util.Map;

/**
 * DSL使用示例
 *
 * @author cjbi
 */
public class DSLExample {

  private final Session session;

  public DSLExample(Session session) {
    this.session = session;
  }

  /**
   * 基本查询示例
   */
  public void basicQueryExample() {
    // 使用字符串字段名查询
    List<Map<String, Object>> users = session.dsl()
      .select("id", "name", "email")
      .from("users")
      .where(Expressions.field("age").gt(18))
      .orderBy("name", Direction.ASC)
      .page(1, 10)
      .execute();

    System.out.println("查询结果: " + users);
  }

  /**
   * 使用实体类查询示例
   */
  public void entityQueryExample() {
    // 使用实体类和方法引用
    List<User> userList = session.dsl()
      .select()
      .from(User.class)
      .where(Expressions.field(User::getName).eq("张三"))
      .execute();

    System.out.println("用户列表: " + userList);
  }

  /**
   * 复杂条件查询示例
   */
  public void complexConditionExample() {
    // 构建复杂条件
    Predicate condition = Expressions.field(User::getAge).gte(18)
      .and(Expressions.field(User::getStatus).eq("active"))
      .and(Expressions.field(User::getName).contains("张"));

    List<User> users = session.dsl()
      .select()
      .from(User.class)
      .where(condition)
      .orderBy("age", Direction.DESC)
      .execute();

    System.out.println("复杂查询结果: " + users);
  }

  /**
   * 统计查询示例
   */
  public void countExample() {
    // 统计符合条件的记录数
    long count = session.dsl()
      .select()
      .from(User.class)
      .where(Expressions.field(User::getAge).gt(25))
      .count();

    System.out.println("年龄大于25的用户数量: " + count);
  }

  /**
   * 单条记录查询示例
   */
  public void singleRecordExample() {
    // 查询单条记录
    User user = session.dsl()
      .select()
      .from(User.class)
      .where(Expressions.field(User::getId).eq(1L))
      .executeOne();

    System.out.println("单个用户: " + user);
  }

  /**
   * 分组查询示例
   */
  public void groupByExample() {
    // 分组查询
    List<Map<String, Object>> result = session.dsl()
      .select("status", "count(*) as user_count")
      .from("users")
      .groupBy("status")
      .execute();

    System.out.println("按状态分组统计: " + result);
  }

  /**
   * 连接查询示例
   */
  public void joinExample() {
    // 连接查询
    List<Map<String, Object>> result = session.dsl()
      .select("u.name", "u.email", "o.order_id")
      .from("users")
      .join(joins -> joins
        .addLeftJoin(join -> join
          .setFrom("orders")
          .setAs("o")
          .setLocalField("u.id")
          .setForeignField("o.user_id")
        )
      )
      .where(Expressions.field("u.status").eq("active"))
      .execute();

    System.out.println("连接查询结果: " + result);
  }

  /**
   * 嵌套查询示例
   */
  public void nestedQueryExample() {
    // 启用嵌套查询
    List<User> users = session.dsl()
      .select()
      .from(User.class)
      .where(Expressions.field(User::getStatus).eq("active"))
      .enableNested()
      .execute();

    System.out.println("嵌套查询结果: " + users);
  }

  /**
   * 字符串条件查询示例
   */
  public void stringConditionExample() {
    // 使用字符串条件
    List<User> users = session.dsl()
      .select()
      .from(User.class)
      .where("age > 18 AND status = 'active'")
      .execute();

    System.out.println("字符串条件查询结果: " + users);
  }

  /**
   * 分页查询示例
   */
  public void paginationExample() {
    // 分页查询
    List<User> users = session.dsl()
      .select()
      .from(User.class)
      .where(Expressions.field(User::getStatus).eq("active"))
      .orderBy("id", Direction.DESC)
      .page(2, 5) // 第2页，每页5条
      .execute();

    System.out.println("分页查询结果: " + users);
  }

  /**
   * 多字段排序示例
   */
  public void multiSortExample() {
    // 多字段排序
    List<User> users = session.dsl()
      .select()
      .from(User.class)
      .where(Expressions.field(User::getAge).gte(18))
      .orderBy("age", Direction.DESC)
      .orderBy("name", Direction.ASC)
      .execute();

    System.out.println("多字段排序结果: " + users);
  }
}
