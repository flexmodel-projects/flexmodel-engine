package tech.wetech.flexmodel.query;

import tech.wetech.flexmodel.query.expr.Predicate;

import java.util.List;
import java.util.function.UnaryOperator;

/**
 * 带类型的DSL查询构建器
 */
public class TypedDSLQueryBuilder<T> {
  private final DSLQueryBuilder delegate;
  private final Class<T> entityClass;

  public TypedDSLQueryBuilder(DSLQueryBuilder delegate, Class<T> entityClass) {
    this.delegate = delegate;
    this.entityClass = entityClass;
  }

  /**
   * 设置要查询的字段
   */
  public TypedDSLQueryBuilder<T> select(UnaryOperator<Query.SelectBuilder> selector) {
    delegate.select(selector);
    return this;
  }

  /**
   * 设置要查询的字段
   */
  public TypedDSLQueryBuilder<T> select(String... fields) {
    delegate.select(fields);
    return this;
  }

  /**
   * 设置过滤条件
   */
  public TypedDSLQueryBuilder<T> where(Predicate predicate) {
    delegate.where(predicate);
    return this;
  }

  /**
   * 设置过滤条件（字符串形式）
   */
  public TypedDSLQueryBuilder<T> where(String filter) {
    delegate.where(filter);
    return this;
  }

  /**
   * 设置过滤条件（按ID）
   */
  public TypedDSLQueryBuilder<T> whereId(Object id) {
    delegate.where(tech.wetech.flexmodel.query.expr.Expressions.field("id").eq(id));
    return this;
  }

  /**
   * 设置排序
   */
  public TypedDSLQueryBuilder<T> orderBy(String field, Direction direction) {
    delegate.orderBy(field, direction);
    return this;
  }

  /**
   * 设置分页
   */
  public TypedDSLQueryBuilder<T> page(int pageNumber, int pageSize) {
    delegate.page(pageNumber, pageSize);
    return this;
  }

  /**
   * 设置限制条数
   */
  public TypedDSLQueryBuilder<T> limit(int limit) {
    delegate.limit(limit);
    return this;
  }

  /**
   * 设置偏移量
   */
  public TypedDSLQueryBuilder<T> offset(int offset) {
    delegate.offset(offset);
    return this;
  }

  /**
   * 设置分组
   */
  public TypedDSLQueryBuilder<T> groupBy(String... fields) {
    delegate.groupBy(fields);
    return this;
  }

  /**
   * 设置连接
   */
  public TypedDSLQueryBuilder<T> join(UnaryOperator<Query.Joins> joinsUnaryOperator) {
    delegate.join(joinsUnaryOperator);
    return this;
  }

  /**
   * 设置左连接
   */
  public TypedDSLQueryBuilder<T> leftJoin(UnaryOperator<Query.Joins> joinsUnaryOperator) {
    delegate.leftJoin(joinsUnaryOperator);
    return this;
  }

  /**
   * 启用嵌套查询
   */
  public TypedDSLQueryBuilder<T> enableNested() {
    delegate.enableNested();
    return this;
  }

  /**
   * 执行查询并返回指定类型的结果（自动使用from中指定的类型）
   */
  public List<T> execute() {
    return delegate.execute(entityClass);
  }

  /**
   * 执行查询并返回指定类型的结果（可以覆盖from中指定的类型）
   */
  public <R> List<R> execute(Class<R> resultType) {
    return delegate.execute(resultType);
  }

  /**
   * 执行查询并返回单个结果（自动使用from中指定的类型）
   */
  public T executeOne() {
    return delegate.executeOne(entityClass);
  }

  /**
   * 执行查询并返回单个结果（可以覆盖from中指定的类型）
   */
  public <R> R executeOne(Class<R> resultType) {
    return delegate.executeOne(resultType);
  }

  /**
   * 统计记录数
   */
  public long count() {
    return delegate.count();
  }

  /**
   * 检查是否存在记录
   */
  public boolean exists() {
    return delegate.exists();
  }
}
