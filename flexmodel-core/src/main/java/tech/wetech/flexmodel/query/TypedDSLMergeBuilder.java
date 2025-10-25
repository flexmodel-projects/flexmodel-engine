package tech.wetech.flexmodel.query;

import tech.wetech.flexmodel.reflect.ReflectionUtils;

import java.util.Map;

/**
 * 带类型的DSL合并构建器
 */
public class TypedDSLMergeBuilder<T> {
  private final DSLMergeBuilder delegate;
  private final Class<T> entityClass;
  private Map<String, Object> dataMap;
  private T values;

  public TypedDSLMergeBuilder(DSLMergeBuilder delegate, Class<T> entityClass) {
    this.delegate = delegate;
    this.entityClass = entityClass;
  }

  /**
   * 设置要合并的值
   */
  public TypedDSLMergeBuilder<T> values(T values) {
    this.values = values;
    dataMap = ReflectionUtils.toClassBean(values, Map.class);
    delegate.values(dataMap);
    return this;
  }

  /**
   * 执行合并操作
   */
  public int execute() {
    try {
      return delegate.execute();
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      if (dataMap != null && values != null) {
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
}
