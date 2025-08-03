package tech.wetech.flexmodel.query;

import tech.wetech.flexmodel.annotation.ModelClass;
import tech.wetech.flexmodel.session.Session;

/**
 * DSL插入构建器
 */
public class DSLInsertBuilder {
  private final Session session;
  private String modelName;
  private Class<?> entityClass;
  private Object values;

  public DSLInsertBuilder(Session session) {
    this.session = session;
  }

  /**
   * 设置要插入的模型名称
   */
  public DSLInsertBuilder insertInto(String modelName) {
    this.modelName = modelName;
    this.entityClass = null;
    return this;
  }

  /**
   * 设置要插入的实体类
   */
  public <T> TypedDSLInsertBuilder<T> insertInto(Class<T> entityClass) {
    this.entityClass = entityClass;
    this.modelName = getModelNameFromClass(entityClass);
    return new TypedDSLInsertBuilder<>(this, entityClass);
  }

  /**
   * 设置要插入的值
   */
  public DSLInsertBuilder values(Object values) {
    this.values = values;
    return this;
  }

  /**
   * 执行插入操作
   */
  public int execute() {
    if (modelName == null) {
      throw new IllegalStateException("Model name or entity class must be specified using insertInto() method");
    }
    if (values == null) {
      throw new IllegalStateException("Values must be specified using values() method");
    }
    return session.data().insert(modelName, values);
  }

  /**
   * 从实体类获取模型名称
   */
  private String getModelNameFromClass(Class<?> entityClass) {
    ModelClass modelClass = entityClass.getAnnotation(ModelClass.class);
    if (modelClass != null) {
      return modelClass.value();
    }
    return entityClass.getSimpleName();
  }
}
