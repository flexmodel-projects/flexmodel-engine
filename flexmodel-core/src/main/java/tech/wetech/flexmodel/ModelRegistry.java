package tech.wetech.flexmodel;


import tech.wetech.flexmodel.model.SchemaObject;
import tech.wetech.flexmodel.session.AbstractSessionContext;

import java.util.List;
import java.util.Set;

/**
 * 模型注册表
 * @author cjbi
 */
public interface ModelRegistry {

  /**
   * 从数据库中加载模型
   *
   * @param sqlContext
   * @return
   */
  List<SchemaObject> loadFromDatabase(AbstractSessionContext sqlContext);

  /**
   * 从数据库中加载模型
   *
   * @param sqlContext
   * @param includes
   * @return
   */
  List<SchemaObject> loadFromDatabase(AbstractSessionContext sqlContext, Set<String> includes);

  /**
   * 获取所有已注册的模型
   *
   * @param schemaName
   * @return
   */
  List<SchemaObject> getAllRegistered(String schemaName);

  /**
   * 注销模型
   *
   * @param schemaName
   */
  void unregister(String schemaName);

  /**
   * 注销模型
   *
   * @param schemaName
   * @param modelName
   */
  void unregister(String schemaName, String modelName);

  /**
   * 注册模型
   *
   * @param schemaName
   * @param object
   */
  void register(String schemaName, SchemaObject object);

  /**
   * 获取已注册的模型
   *
   * @param schemaName
   * @param modelName
   * @return
   */
  SchemaObject getRegistered(String schemaName, String modelName);

}
