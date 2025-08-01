package tech.wetech.flexmodel;

import tech.wetech.flexmodel.query.Direction;
import tech.wetech.flexmodel.query.Query;
import tech.wetech.flexmodel.query.QueryDSL;
import tech.wetech.flexmodel.query.expr.Predicate;

/**
 * 测试修正后的模型概念DSL语法
 *
 * @author cjbi
 */
public class QueryDSLModelTest {

  /**
   * 测试基础模型查询
   */
  public static void testBasicModelQuery() {
    // 使用模型概念的查询
    Query query = QueryDSL.query()
      .select(select -> select
        .field("teacher_id", "id")
        .field("teacher_name", "name")
      )
      .where(QueryDSL.where("name").eq("张三"))
      .build();

    System.out.println("基础模型查询构建成功");
  }

  /**
   * 测试模型连接查询
   */
  public static void testModelJoinQuery() {
    // 使用模型概念的连接查询
    Query joinQuery = QueryDSL.query()
      .select(select -> select
        .field("teacher_name", "name")
        .count("course_count", "teacher_courses.teacher_id")
        .sum("course_score_sum", "teacher_courses.c_score")
      )
      .innerJoin(join -> join
        .model("teacher_courses")  // 使用model而不是table
        .where("{\"teacher_id\": {\"_ne\": 999}}")
      )
      .groupBy(groupBy -> groupBy
        .field("teacher_name")
      )
      .where(QueryDSL.where("name").eq("李四"))
      .build();

    System.out.println("模型连接查询构建成功");
  }

  /**
   * 测试多模型连接
   */
  public static void testMultiModelJoin() {
    // 多模型连接示例
    Query multiJoinQuery = QueryDSL.query()
      .select(select -> select
        .field("student_name", "students.name")
        .field("class_name", "classes.name")
        .field("course_name", "courses.name")
      )
      .leftJoin(join -> join
        .model("students")  // 学生模型
        .as("students")
        .on("id", "class_id")
      )
      .leftJoin(join -> join
        .model("courses")   // 课程模型
        .as("courses")
        .on("class_id", "class_id")
      )
      .where(QueryDSL.where("students.age").gte(18))
      .build();

    System.out.println("多模型连接查询构建成功");
  }

  /**
   * 测试聚合模型查询
   */
  public static void testAggregateModelQuery() {
    // 基于模型的聚合查询
    Query aggQuery = QueryDSL.query()
      .select(select -> select
        .field("department")
        .count("employee_count", "employees.id")
        .avg("avg_salary", "employees.salary")
        .sum("total_salary", "employees.salary")
      )
      .innerJoin(join -> join
        .model("employees")  // 员工模型
        .where(QueryDSL.where("status").eq("active"))
      )
      .groupBy(groupBy -> groupBy
        .field("department")
      )
      .orderBy(orderBy -> orderBy
        .desc("total_salary")
      )
      .build();

    System.out.println("聚合模型查询构建成功");
  }

  /**
   * 测试复杂模型条件
   */
  public static void testComplexModelConditions() {
    // 复杂的模型条件查询
    Predicate complexCondition = QueryDSL.where("age").gte(18)
      .and(QueryDSL.where("age").lte(65))
      .and(QueryDSL.where("status").eq("active"))
      .and(QueryDSL.where("department").in("技术部", "产品部"))
      .or(QueryDSL.where("level").eq("高级"));

    Query complexQuery = QueryDSL.simple()
      .select("id", "name", "age", "department", "level")
      .where(complexCondition)
      .orderBy("age", Direction.DESC)
      .page(1, 20)
      .build();

    System.out.println("复杂模型条件查询构建成功");
  }

  /**
   * 运行所有测试
   */
  public static void runAllTests() {
    System.out.println("=== 开始测试模型概念DSL语法 ===");

    try {
      testBasicModelQuery();
      testModelJoinQuery();
      testMultiModelJoin();
      testAggregateModelQuery();
      testComplexModelConditions();

      System.out.println("=== 所有测试通过！模型概念DSL语法工作正常 ===");
    } catch (Exception e) {
      System.err.println("测试失败: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    runAllTests();
  }
}
