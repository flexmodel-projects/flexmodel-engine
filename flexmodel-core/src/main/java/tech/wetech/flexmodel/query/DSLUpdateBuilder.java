package tech.wetech.flexmodel.query;

import tech.wetech.flexmodel.annotation.ModelClass;
import tech.wetech.flexmodel.query.expr.Predicate;
import tech.wetech.flexmodel.session.Session;

import java.util.HashMap;
import java.util.Map;

/**
 * DSL更新构建器
 */
public class DSLUpdateBuilder {
  private final Session session;
  private String modelName;
  private Class<?> entityClass;
  private Map<String, Object> values = new HashMap<>();
  private String whereCondition;
  private Object whereId;

  public DSLUpdateBuilder(Session session) {
    this.session = session;
  }

  /**
   * 设置要更新的模型名称
   */
  public DSLUpdateBuilder update(String modelName) {
    this.modelName = modelName;
    this.entityClass = null;
    return this;
  }

  /**
   * 设置要更新的实体类
   */
  public <T> TypedDSLUpdateBuilder<T> update(Class<T> entityClass) {
    this.entityClass = entityClass;
    this.modelName = getModelNameFromClass(entityClass);
    return new TypedDSLUpdateBuilder<>(this, entityClass);
  }

  /**
   * 设置单个字段的值
   */
  public DSLUpdateBuilder set(String field, Object value) {
    this.values.put(field, value);
    return this;
  }

  /**
   * 设置多个字段的值
   */
  public DSLUpdateBuilder values(Map<String, Object> values) {
    this.values.putAll(values);
    return this;
  }

  /**
   * 设置WHERE条件（字符串形式）
   */
  public DSLUpdateBuilder where(String condition) {
    this.whereCondition = condition;
    this.whereId = null;
    return this;
  }

  /**
   * 设置WHERE条件（Predicate形式）
   */
  public DSLUpdateBuilder where(Predicate predicate) {
    this.whereCondition = predicate.toJsonString();
    this.whereId = null;
    return this;
  }

  /**
   * 设置WHERE条件（按ID）
   */
  public DSLUpdateBuilder whereId(Object id) {
    this.whereId = id;
    this.whereCondition = null;
    return this;
  }

  /**
   * 执行更新操作
   */
  public int execute() {
    if (modelName == null) {
      throw new IllegalStateException("Model name or entity class must be specified using update() method");
    }
    if (values.isEmpty()) {
      throw new IllegalStateException("Values must be specified using set() or values() method");
    }

    if (whereId != null) {
      return session.updateById(modelName, values, whereId);
    } else if (whereCondition != null) {
      return session.update(modelName, values, whereCondition);
    } else {
      throw new IllegalStateException("WHERE condition must be specified using where() or whereId() method");
    }
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
