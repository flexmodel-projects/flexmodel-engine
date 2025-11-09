package tech.wetech.flexmodel.query;

import tech.wetech.flexmodel.JsonUtils;
import tech.wetech.flexmodel.reflect.LazyObjProxy;
import tech.wetech.flexmodel.reflect.ReflectionUtils;
import tech.wetech.flexmodel.session.Session;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * DSL查询构建器
 */
public class DSLQueryBuilder {
  private final Session session;
  private String modelName;
  private final Query query;

  public DSLQueryBuilder(Session session) {
    this.session = session;
    this.query = new Query();
  }

  /**
   * 设置要查询的字段
   */
  public DSLQueryBuilder select(UnaryOperator<Query.SelectBuilder> selector) {
    Query.SelectBuilder selectBuilder = new Query.SelectBuilder();
    selector.apply(selectBuilder);
    Query.Projection projection = query.getProjection();
    if (projection == null) {
      projection = new Query.Projection();
      query.setProjection(projection);
    }
    final Query.Projection finalProjection = projection;
    selectBuilder.getFields().forEach(field ->
      finalProjection.addField(field.alias, field.expression));
    query.setProjection(finalProjection);
    return this;
  }

  /**
   * 设置要查询的字段，不传则查询所有字段
   */
  public DSLQueryBuilder select(String... fields) {
    if (fields.length == 0) {
      // 不存在则查询所有
      return this;
    }
    Query.Projection projection = new Query.Projection();
    for (String field : fields) {
      projection.addField(field, new Query.QueryField(field));
    }
    query.setProjection(projection);
    return this;
  }

  /**
   * 设置要查询的模型名称
   */
  public DSLQueryBuilder from(String modelName) {
    this.modelName = modelName;
    return this;
  }

  public <T> TypedDSLQueryBuilder<T> from(Class<T> entityClass) {
    this.modelName = ReflectionUtils.getModelNameFromClass(entityClass);
    return new TypedDSLQueryBuilder<>(this, entityClass);
  }

  /**
   * 设置过滤条件
   */
  public DSLQueryBuilder where(Predicate predicate) {
    query.setFilter(predicate.toJsonString());
    return this;
  }

  /**
   * 设置过滤条件（字符串形式）
   */
  public DSLQueryBuilder where(String filter) {
    query.setFilter(filter);
    return this;
  }

  /**
   * 设置排序
   */
  public DSLQueryBuilder orderBy(String field, Direction direction) {
    Query.OrderBy sort = new Query.OrderBy();
    if (direction == Direction.ASC) {
      sort.asc(field);
    } else {
      sort.desc(field);
    }
    query.setOrderBy(sort);
    return this;
  }


  public DSLQueryBuilder orderBy(String field) {
    orderBy(field, Direction.ASC);
    return this;
  }

  /**
   * 设置排序
   */
  public DSLQueryBuilder orderByDesc(String field) {
    orderBy(field, Direction.DESC);
    return this;
  }

  /**
   * 设置排序
   */
  public DSLQueryBuilder orderBy(Query.OrderBy sort) {
    query.setOrderBy(sort);
    return this;
  }

  /**
   * 设置分页
   */
  public DSLQueryBuilder page(int pageNumber, int pageSize) {
    Query.Page page = new Query.Page();
    page.setPageNumber(pageNumber);
    page.setPageSize(pageSize);
    query.setPage(page);
    return this;
  }

  public DSLQueryBuilder page(Query.Page page) {
    query.setPage(page);
    return this;
  }

  /**
   * 设置限制条数
   */
  public DSLQueryBuilder limit(int limit) {
    Query.Page page = new Query.Page();
    page.setPageNumber(1);
    page.setPageSize(limit);
    query.setPage(page);
    return this;
  }

  /**
   * 设置偏移量
   */
  public DSLQueryBuilder offset(int offset) {
    // 这里需要计算页码，假设每页大小为offset+1
    int pageSize = offset + 1;
    int pageNumber = 1;
    Query.Page page = new Query.Page();
    page.setPageNumber(pageNumber);
    page.setPageSize(pageSize);
    query.setPage(page);
    return this;
  }

  /**
   * 设置分组
   */
  public DSLQueryBuilder groupBy(String... fields) {
    Query.GroupBy groupBy = new Query.GroupBy();
    for (String field : fields) {
      groupBy.addField(field);
    }
    query.setGroupBy(groupBy);
    return this;
  }

  /**
   * 设置连接
   */
  public DSLQueryBuilder join(UnaryOperator<Query.Joins> joinsUnaryOperator) {
    Query.Joins joins = new Query.Joins();
    query.setJoins(joinsUnaryOperator.apply(joins));
    return this;
  }

  /**
   * 设置左连接
   */
  public DSLQueryBuilder leftJoin(UnaryOperator<Query.Joins> joinsUnaryOperator) {
    Query.Joins joins = new Query.Joins();
    query.setJoins(joinsUnaryOperator.apply(joins));
    return this;
  }

  /**
   * 启用嵌套查询
   */
  public DSLQueryBuilder enableNested() {
    query.setNestedEnabled(true);
    return this;
  }


  /**
   * 执行查询并返回Map结果
   */
  @SuppressWarnings("all")
  public List<Map<String, Object>> execute() {
    List list = session.data().find(modelName, query);
    return list;
  }


  /**
   * 执行查询并返回Map结果
   */
  @SuppressWarnings("all")
  public <T> List<T> execute(Class<T> resultType) {
    if (modelName == null) {
      throw new IllegalStateException("Model name or entity class must be specified using from() method");
    }
    List<Map<String, Object>> list = execute();
    List<T> typedResults = JsonUtils.convertValueList(list, resultType);
    return LazyObjProxy.createProxyList(typedResults, modelName, session);
  }

  /**
   * 执行查询并返回单个Map结果
   */
  public Map<String, Object> executeOne() {
    List<Map<String, Object>> results = execute();
    return results.isEmpty() ? null : results.getFirst();
  }

  /**
   * 统计记录数
   */
  public long count() {
    if (modelName == null) {
      throw new IllegalStateException("Model name or entity class must be specified using from() method");
    }
    return session.data().count(modelName, query);
  }

  /**
   * 检查是否存在记录
   */
  public boolean exists() {
    return count() > 0;
  }

}
