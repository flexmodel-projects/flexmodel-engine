package tech.wetech.flexmodel;

import java.util.List;
import java.util.Set;
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

  List<Model> syncModels(Set<String> modelNames);

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
   * @param entity
   * @return
   */
  Entity createEntity(Entity entity);

  View createView(String viewName, String viewOn, Query query);

  NativeQueryModel createNativeQueryModel(NativeQueryModel model);

  /**
   * 创建字段
   *
   * @param field
   */
  TypedField<?, ?> createField(TypedField<?, ?> field);

  TypedField<?, ?> modifyField(TypedField<?, ?> field);

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
  Index createIndex(Index index);

  /**
   * 删除索引
   *
   * @param entityName 模型名称
   * @param indexName  索引名称
   */
  void dropIndex(String entityName, String indexName);

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
    return createEntity(entity);
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

  default NativeQueryModel createNativeQueryModel(String modelName, UnaryOperator<NativeQueryModel> modelUnaryOperator) {
    NativeQueryModel model = new NativeQueryModel(modelName);
    modelUnaryOperator.apply(model);
    return createNativeQueryModel(model);
  }

}
