package tech.wetech.flexmodel.query;

/**
 * Query类使用示例
 * 展示如何使用合并后的Query类进行查询构建
 */
public class QueryExample {

  public static void main(String[] args) {
    // 示例1：使用Query.Builder模式（推荐）
    Query query1 = Query.Builder.create()
      .where("status = 'active'")
      .select("id", "name", "email")
      .innerJoin(join -> join
        .model("user")
        .as("u")
        .on("id", "user_id")
        .where("u.status = 'active'")
      )
      .leftJoin(join -> join
        .model("order")
        .as("o")
        .on("user_id", "id")
      )
      .groupBy(groupBy -> groupBy.field("user_id"))
      .orderBy(orderBy -> orderBy.asc("name").desc("created_at"))
      .page(1, 20)
      .build();

    // 示例2：使用复杂的select语句
    Query query2 = Query.Builder.create()
      .select(select -> select
        .field("id")
        .field("name", "user_name")
        .count("id", "total_count")
        .sum("amount", "total_amount")
        .avg("score", "avg_score")
        .max("created_at", "latest_date")
        .min("created_at", "earliest_date")
        .dateFormat("created_at", "%Y-%m-%d", "formatted_date")
      )
      .where("status = 'active'")
      .build();

    // 示例3：使用字符串条件
    Query query3 = Query.Builder.create()
      .where("status = 'active'")
      .select("id", "name")
      .build();

    // 示例4：直接使用Query类的setter方法
    Query query4 = new Query();
    query4.setFilter("status = 'active'");
    Query.Projection projection4 = new Query.Projection();
    projection4.addField("id", Query.field("id"));
    query4.setProjection(projection4);

    Query.Joins joins4 = new Query.Joins();
    joins4.addInnerJoin(join -> join
      .setFrom("user")
      .setAs("u")
      .setLocalField("id")
      .setForeignField("user_id")
    );
    query4.setJoins(joins4);

    // 示例5：使用静态辅助方法
    Query query5 = Query.Builder.create()
      .select(select -> select
        .field("id")
        .field("name")
        .field("total", Query.count(Query.field("id")))
        .field("amount", Query.sum(Query.field("price")))
      )
      .where("category = 'electronics'")
      .build();

    System.out.println("Query示例创建完成！");
    System.out.println("Query1: " + query1);
    System.out.println("Query2: " + query2);
    System.out.println("Query3: " + query3);
    System.out.println("Query4: " + query4);
    System.out.println("Query5: " + query5);
  }

  /**
   * 展示不同的查询模式
   */
  public static void demonstrateQueryPatterns() {
    System.out.println("=== Query使用模式示例 ===");

    // 模式1：简单查询
    System.out.println("1. 简单查询：");
    Query simpleQuery = Query.Builder.create()
      .select("id", "name")
      .where("status = 'active'")
      .build();

    // 模式2：带连接的查询
    System.out.println("2. 带连接的查询：");
    Query joinQuery = Query.Builder.create()
      .select("u.id", "u.name", "o.order_id")
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

    // 模式3：聚合查询
    System.out.println("3. 聚合查询：");
    Query aggregateQuery = Query.Builder.create()
      .select(select -> select
        .count("id", "total_users")
        .sum("amount", "total_amount")
        .avg("score", "avg_score")
      )
      .groupBy(groupBy -> groupBy.field("category"))
      .build();

    // 模式4：分页排序查询
    System.out.println("4. 分页排序查询：");
    Query pagedQuery = Query.Builder.create()
      .select("id", "name", "created_at")
      .orderBy(orderBy -> orderBy.asc("name").desc("created_at"))
      .page(1, 10)
      .build();

    System.out.println("所有查询模式示例完成！");
  }
}
