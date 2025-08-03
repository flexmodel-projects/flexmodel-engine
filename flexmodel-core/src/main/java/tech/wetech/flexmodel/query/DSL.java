package tech.wetech.flexmodel.query;

import tech.wetech.flexmodel.annotation.ModelClass;
import tech.wetech.flexmodel.session.Session;

import java.util.function.UnaryOperator;

/**
 * @author cjbi
 */
public
class DSL {

  private final Session session;

  public DSL(Session session) {
    this.session = session;
  }

  /**
   * 创建DSL查询构建器
   *
   * @return DSL查询构建器
   */
  public DSLQueryBuilder selectFrom(String modelName) {
    return new DSLQueryBuilder(session).select().from(modelName);
  }

  /**
   * 创建DSL查询构建器
   *
   * @return DSL查询构建器
   */
  public <T> TypedDSLQueryBuilder<T> selectFrom(Class<T> entityClass) {
    return new TypedDSLQueryBuilder<>(selectFrom(getModelNameFromClass(entityClass)), entityClass);
  }

  /**
   * 设置要查询的字段
   */
  public DSLQueryBuilder select(UnaryOperator<Query.SelectBuilder> selector) {
    return new DSLQueryBuilder(session).select(selector);
  }

  /**
   * 设置要查询的字段
   */
  public DSLQueryBuilder select(String... fields) {
    return new DSLQueryBuilder(session).select(fields);
  }

  /**
   * 设置要插入的模型名称
   */
  public DSLInsertBuilder insertInto(String modelName) {
    return new DSLInsertBuilder(session).insertInto(modelName);
  }

  /**
   * 设置要插入的实体类
   */
  public <T> TypedDSLInsertBuilder<T> insertInto(Class<T> entityClass) {
    return new TypedDSLInsertBuilder<>(insertInto(getModelNameFromClass(entityClass)), entityClass);
  }

  /**
   * 设置要合并的模型名称
   */
  public DSLMergeBuilder mergeInto(String modelName) {
    return new DSLMergeBuilder(session).mergeInto(modelName);
  }

  /**
   * 设置要合并的实体类
   */
  public <T> TypedDSLMergeBuilder<T> mergeInto(Class<T> entityClass) {
    return new TypedDSLMergeBuilder<>(mergeInto(getModelNameFromClass(entityClass)), entityClass);
  }


  /**
   * 设置要更新的模型名称
   */
  public DSLUpdateBuilder update(String modelName) {
    return new DSLUpdateBuilder(session).update(modelName);
  }

  /**
   * 设置要更新的实体类
   */
  public <T> TypedDSLUpdateBuilder<T> update(Class<T> entityClass) {
    return new TypedDSLUpdateBuilder<>(update(getModelNameFromClass(entityClass)), entityClass);
  }

  /**
   * 设置要删除的模型名称
   */
  public DSLDeleteBuilder deleteFrom(String modelName) {
    return new DSLDeleteBuilder(session).deleteFrom(modelName);
  }

  /**
   * 设置要删除的实体类
   */
  public <T> TypedDSLDeleteBuilder<T> deleteFrom(Class<T> entityClass) {
    return new TypedDSLDeleteBuilder<>(deleteFrom(getModelNameFromClass(entityClass)), entityClass);
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
