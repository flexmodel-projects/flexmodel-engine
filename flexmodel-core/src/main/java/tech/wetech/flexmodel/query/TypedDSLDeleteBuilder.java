package tech.wetech.flexmodel.query;

/**
 * 带类型的DSL删除构建器
 */
public class TypedDSLDeleteBuilder<T> {
  private final DSLDeleteBuilder delegate;
  private final Class<T> entityClass;

  public TypedDSLDeleteBuilder(DSLDeleteBuilder delegate, Class<T> entityClass) {
    this.delegate = delegate;
    this.entityClass = entityClass;
  }

  /**
   * 设置WHERE条件（字符串形式）
   */
  public TypedDSLDeleteBuilder<T> where(String condition) {
    delegate.where(condition);
    return this;
  }

  /**
   * 设置WHERE条件（Predicate形式）
   */
  public TypedDSLDeleteBuilder<T> where(Predicate predicate) {
    delegate.where(predicate);
    return this;
  }

  /**
   * 设置WHERE条件（按ID）
   */
  public TypedDSLDeleteBuilder<T> whereId(Object id) {
    delegate.whereId(id);
    return this;
  }

  /**
   * 执行删除操作
   */
  public int execute() {
    return delegate.execute();
  }
}
