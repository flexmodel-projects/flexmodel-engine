package tech.wetech.flexmodel.query;

import tech.wetech.flexmodel.reflect.ReflectionUtils;
import tech.wetech.flexmodel.session.Session;

/**
 * DSL删除构建器
 */
public class DSLDeleteBuilder {
  private final Session session;
  private String modelName;
  private Class<?> entityClass;
  private String whereCondition;
  private Object whereId;

  public DSLDeleteBuilder(Session session) {
    this.session = session;
  }

  /**
   * 设置要删除的模型名称
   */
  public DSLDeleteBuilder deleteFrom(String modelName) {
    this.modelName = modelName;
    this.entityClass = null;
    return this;
  }

  /**
   * 设置要删除的实体类
   */
  public <T> TypedDSLDeleteBuilder<T> deleteFrom(Class<T> entityClass) {
    this.entityClass = entityClass;
    this.modelName = ReflectionUtils.getModelNameFromClass(entityClass);
    return new TypedDSLDeleteBuilder<>(this, entityClass);
  }

  /**
   * 设置WHERE条件（字符串形式）
   */
  public DSLDeleteBuilder where(String condition) {
    this.whereCondition = condition;
    this.whereId = null;
    return this;
  }

  /**
   * 设置WHERE条件（Predicate形式）
   */
  public DSLDeleteBuilder where(Predicate predicate) {
    this.whereCondition = predicate.toJsonString();
    this.whereId = null;
    return this;
  }

  /**
   * 设置WHERE条件（按ID）
   */
  public DSLDeleteBuilder whereId(Object id) {
    this.whereId = id;
    this.whereCondition = null;
    return this;
  }

  /**
   * 执行删除操作
   */
  public int execute() {
    if (modelName == null) {
      throw new IllegalStateException("Model name or entity class must be specified using deleteFrom() method");
    }

    if (whereId != null) {
      return session.data().deleteById(modelName, whereId);
    } else if (whereCondition != null) {
      return session.data().delete(modelName, whereCondition);
    } else {
      // 如果没有WHERE条件，删除所有记录
      return session.data().deleteAll(modelName);
    }
  }

}
