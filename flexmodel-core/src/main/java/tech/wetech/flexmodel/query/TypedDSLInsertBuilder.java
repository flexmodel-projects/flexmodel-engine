package tech.wetech.flexmodel.query;

import java.util.HashMap;
import java.util.Map;

/**
 * 带类型的DSL插入构建器
 */
public class TypedDSLInsertBuilder<T> {
  private final DSLInsertBuilder delegate;
  private final Class<T> entityClass;

  public TypedDSLInsertBuilder(DSLInsertBuilder delegate, Class<T> entityClass) {
    this.delegate = delegate;
    this.entityClass = entityClass;
  }

  /**
   * 设置要插入的值
   */
  public TypedDSLInsertBuilder<T> values(Map<String, Object> values) {
    delegate.values(values);
    return this;
  }

  /**
   * 设置要插入的值（使用实体对象）
   */
  public TypedDSLInsertBuilder<T> values(T entity) {
    // 这里需要将实体对象转换为Map
    // 简化实现，实际项目中可能需要更复杂的对象转换逻辑
    Map<String, Object> values = new HashMap<>();
    // 使用反射获取字段值
    try {
      for (java.lang.reflect.Field field : entityClass.getDeclaredFields()) {
        field.setAccessible(true);
        Object value = field.get(entity);
        if (value != null) {
          values.put(field.getName(), value);
        }
      }
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Failed to convert entity to map", e);
    }
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
