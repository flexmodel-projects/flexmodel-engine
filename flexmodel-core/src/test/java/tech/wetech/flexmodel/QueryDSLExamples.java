package tech.wetech.flexmodel;

import tech.wetech.flexmodel.query.Query;
import tech.wetech.flexmodel.query.QueryBuilder;
import tech.wetech.flexmodel.query.expr.Expressions;
import tech.wetech.flexmodel.query.expr.Predicate;

/**
 * QueryDSL使用示例
 * 展示优化后的DSL语法的使用方法
 *
 * @author cjbi
 */
public class QueryDSLExamples {

  /**
   * 基础查询示例
   */
  public static void basicQueryExample() {

    // 优化后的语法（简洁）
    Query newQuery = QueryBuilder.create()
      .select(select -> select
        .field("teacher_id", "id")
        .field("teacher_name", "name")
      )
      .where(Expressions.field("name").eq("张三").or(Expressions.field("name").eq("李四")))
      .build();

    // 更简洁的语法
    Query simpleQuery = QueryBuilder.create()
      .select("teacher_id", "teacher_name")
      .where(Expressions.field("name").eq("张三"))
      .orderBy(orderBy -> orderBy.asc("name"))
      .page(1, 10)
      .build();
  }

  /**
   * 聚合查询示例
   */
  public static void aggregationQueryExample() {

    // 优化后的语法
    Query newAggQuery = QueryBuilder.create()
      .select(select -> select
        .field("teacher_name", "name")
        .count("course_count", "teacher_courses.teacher_id")
        .sum("course_score_sum", "teacher_courses.c_score")
      )
      .innerJoin(join -> join
        .model("teacher_courses")
        .where("{\"teacher_id\": {\"_ne\": 999}}")
      )
      .groupBy(groupBy -> groupBy
        .field("teacher_name")
      )
      .where(Expressions.field("name").eq("李四"))
      .build();
  }

  /**
   * 日期函数查询示例
   */
  public static void dateFunctionQueryExample() {
    // 优化后的语法
    Query newDateQuery = QueryBuilder.create()
      .select(select -> select
        .dateFormat("year", "birthday", "yyyy-MM-dd")
        .count("user_count", "id")
      )
      .groupBy(groupBy -> groupBy
        .field("year")
      )
      .build();

    // 使用日期函数构建器
    Query dateQuery = QueryBuilder.create()
      .select(select -> select
        .field("year", QueryBuilder.dateFormat(QueryBuilder.field("birthday"), "yyyy-MM-dd"))
        .field("day_of_week", QueryBuilder.dayOfWeek(QueryBuilder.field("birthday")))
        .field("day_of_month", QueryBuilder.dayOfMonth(QueryBuilder.field("birthday")))
        .field("day_of_year", QueryBuilder.dayOfYear(QueryBuilder.field("birthday")))
      )
      .build();
  }

  /**
   * 复杂条件查询示例
   */
  public static void complexConditionQueryExample() {
    // 优化后的语法
    Predicate newCondition = Expressions.field("age").gte(18)
      .and(Expressions.field("age").lte(65))
      .and(Expressions.field("status").eq("active"))
      .and(Expressions.field("name").contains("张").or(Expressions.field("name").contains("李")));

    Query query = QueryBuilder.create()
      .select("id", "name", "age", "status")
      .where(newCondition)
      .orderBy(orderBy -> orderBy.desc("age"))
      .page(1, 20)
      .build();
  }

  /**
   * 连接查询示例
   */
  public static void joinQueryExample() {
    // 优化后的语法
    Query newJoinQuery = QueryBuilder.create()
      .select(select -> select
        .field("student_name", "students.name")
        .field("class_name", "classes.name")
      )
      .leftJoin(join -> join
        .model("students")
        .as("students")
        .on("id", "class_id")
      )
      .build();
  }

  /**
   * 分页和排序示例
   */
  public static void paginationAndSortingExample() {

    // 优化后的语法
    Query newPageQuery = QueryBuilder.create()
      .orderBy(orderBy -> orderBy
        .asc("name")
        .desc("age")
      )
      .page(2, 10)
      .build();

    // 简单语法
    Query simplePageQuery = QueryBuilder.create()
      .select("id", "name", "age")
      .orderBy(orderBy -> orderBy
        .asc("name")
        .desc("age")
      )
      .page(2, 10)
      .build();
  }

  /**
   * 嵌套查询示例
   */
  public static void nestedQueryExample() {

    // 优化后的语法
    Query newNestedQuery = QueryBuilder.create()
      .enableNested()
      .select(select -> select
        .field("id")
        .field("name")
      )
      .build();
  }

  /**
   * 聚合函数示例
   */
  public static void aggregationFunctionsExample() {
    // 使用聚合函数构建器
    Query aggQuery = QueryBuilder.create()
      .select(select -> select
        .field("category")
        .field("total_count", QueryBuilder.count(QueryBuilder.field("id")))
        .field("total_amount", QueryBuilder.sum(QueryBuilder.field("amount")))
        .field("avg_amount", QueryBuilder.avg(QueryBuilder.field("amount")))
        .field("max_amount", QueryBuilder.max(QueryBuilder.field("amount")))
        .field("min_amount", QueryBuilder.min(QueryBuilder.field("amount")))
      )
      .groupBy(groupBy -> groupBy
        .field("category")
      )
      .build();
  }
}
