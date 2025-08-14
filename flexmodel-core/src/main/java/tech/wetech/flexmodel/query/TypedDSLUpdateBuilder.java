package tech.wetech.flexmodel.query;

/**
 * 带类型的DSL更新构建器
 */
public class TypedDSLUpdateBuilder<T> {
  private final DSLUpdateBuilder delegate;
  private final Class<T> entityClass;

  public TypedDSLUpdateBuilder(DSLUpdateBuilder delegate, Class<T> entityClass) {
    this.delegate = delegate;
    this.entityClass = entityClass;
  }

  /**
   * 设置单个字段的值
   */
  public TypedDSLUpdateBuilder<T> set(String field, Object value) {
    delegate.set(field, value);
    return this;
  }

  /**
   * 设置多个字段的值
   */
  public TypedDSLUpdateBuilder<T> values(T values) {
    delegate.values(values);
    return this;
  }


  /**
   * 设置WHERE条件（字符串形式）
   */
  public TypedDSLUpdateBuilder<T> where(String condition) {
    delegate.where(condition);
    return this;
  }

  /**
   * 设置WHERE条件（Predicate形式）
   */
  public TypedDSLUpdateBuilder<T> where(Predicate predicate) {
    delegate.where(predicate);
    return this;
  }

  /**
   * 设置WHERE条件（按ID）
   */
  public TypedDSLUpdateBuilder<T> whereId(Object id) {
    delegate.whereId(id);
    return this;
  }

  /**
   * 执行更新操作
   */
  public int execute() {
    return delegate.execute();
  }
}
