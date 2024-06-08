package tech.wetech.flexmodel;

import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Schema执行器
 *
 * @author cjbi
 */
public interface SchemaOperations {

  /**
   * 同步模型变更
   */
  List<Model> syncModels();

  /**
   * 获取所有模型
   *
   * @return 模型列表
   */
  List<Model> getAllModels();

  /**
   * 获取模型
   *
   * @param modelName 模型名称
   * @return 实体
   */
  Model getModel(String modelName);

  /**
   * 删除模型
   *
   * @param modelName 模型名称
   */
  void dropModel(String modelName);

  /**
   * 创建实体
   *
   * @param modelName
   * @param entity
   * @return
   */
  Entity createEntity(String modelName, Entity entity);

  View createView(String viewName, String viewOn, Query query);

  /**
   * 创建字段
   *
   * @param field
   */
  TypedField<?, ?> createField(String modelName, TypedField<?, ?> field);

  /**
   * 删除字段
   *
   * @param entityName 模型名称
   * @param fieldName  字段名称
   */
  void dropField(String entityName, String fieldName);

  /**
   * 创建索引
   *
   * @param index
   */
  Index createIndex(String modelName, Index index);

  /**
   * 删除索引
   *
   * @param modelName 模型名称
   * @param indexName 索引名称
   */
  void dropIndex(String modelName, String indexName);

  /**
   * 创建序列
   *
   * @param sequenceName
   * @param initialValue
   * @param incrementSize
   */
  void createSequence(String sequenceName, int initialValue, int incrementSize);

  /**
   * 删除序列
   *
   * @param sequenceName
   */
  void dropSequence(String sequenceName);

  /**
   * 获取序列下一个值
   *
   * @param sequenceName 序列名称
   * @return 序列值
   */
  long getSequenceNextVal(String sequenceName);

  /**
   * 创建实体
   *
   * @param modelName
   * @param entityUnaryOperator
   * @return
   */
  default Entity createEntity(String modelName, UnaryOperator<Entity> entityUnaryOperator) {
    Entity entity = new Entity(modelName);
    entityUnaryOperator.apply(entity);
    return createEntity(modelName, entity);
  }

  /**
   * 创建视图
   *
   * @param viewName
   * @param queryUnaryOperator
   * @return
   */
  default View createView(String viewName, String viewOn, UnaryOperator<Query> queryUnaryOperator) {
    Query query = new Query();
    queryUnaryOperator.apply(query);
    return createView(viewName, viewOn, query);
  }

}
