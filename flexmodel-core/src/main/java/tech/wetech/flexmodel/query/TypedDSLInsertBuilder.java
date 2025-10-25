package tech.wetech.flexmodel.query;

import tech.wetech.flexmodel.reflect.ReflectionUtils;

import java.util.Map;

/**
 * 带类型的DSL插入构建器
 */
public class TypedDSLInsertBuilder<T> {
  private final DSLInsertBuilder delegate;
  private final Class<T> entityClass;
  private Map<String, Object> dataMap;
  private T values;

  public TypedDSLInsertBuilder(DSLInsertBuilder delegate, Class<T> entityClass) {
    this.delegate = delegate;
    this.entityClass = entityClass;
  }

  /**
   * 设置要插入的值
   */
  public TypedDSLInsertBuilder<T> values(T values) {
    this.values = values;
    dataMap = ReflectionUtils.toClassBean(values, Map.class);
    delegate.values(dataMap);
    return this;
  }

  /**
   * 执行插入操作
   */
  public int execute() {
    try {
      return delegate.execute();
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      dataMap.forEach((field, value) -> {
        try {
          ReflectionUtils.setFieldValue(values, field, value);
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
    }
  }
}
