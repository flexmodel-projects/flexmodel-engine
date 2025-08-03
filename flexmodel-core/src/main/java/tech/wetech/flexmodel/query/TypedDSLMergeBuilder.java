package tech.wetech.flexmodel.query;

/**
 * 带类型的DSL插入构建器
 */
public class TypedDSLMergeBuilder<T> {
  private final DSLMergeBuilder delegate;
  private final Class<T> entityClass;

  public TypedDSLMergeBuilder(DSLMergeBuilder delegate, Class<T> entityClass) {
    this.delegate = delegate;
    this.entityClass = entityClass;
  }

  /**
   * 设置要插入的值
   */
  public TypedDSLMergeBuilder<T> values(Object values) {
    delegate.values(values);
    return this;
  }

  /**
   * 执行插入操作
   */
  public int execute() {
    return delegate.execute();
  }
}
