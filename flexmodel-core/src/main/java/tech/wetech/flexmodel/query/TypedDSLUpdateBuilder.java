package tech.wetech.flexmodel.query;

import tech.wetech.flexmodel.JsonUtils;

import java.util.Map;

/**
 * 带类型的DSL更新构建器
 */
public class TypedDSLUpdateBuilder<T> {
  private final DSLUpdateBuilder delegate;
  private final Class<T> entityClass;
  private Map<String, Object> dataMap;
  private T values;

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

  public <R> TypedDSLUpdateBuilder<T> set(Expressions.SFunction<T, R> getter, Object value) {
    delegate.set(Expressions.getFieldName(getter), value);
    return this;
  }

  /**
   * 设置多个字段的值
   */
  public TypedDSLUpdateBuilder<T> values(T values) {
    this.values = values;
    dataMap = JsonUtils.convertValue(values, Map.class);
    delegate.values(dataMap);
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
    try {
      return delegate.execute();
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      values = JsonUtils.updateValue(values, dataMap);
    }
  }
}
