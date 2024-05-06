package tech.wetech.flexmodel;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * 数据操作
 *
 * @author cjbi
 */
public interface DataOperations {

  /**
   * 插入记录
   *
   * @param modelName 模型名称
   * @param record    记录
   * @return affected rows
   */
  int insert(String modelName, Map<String, Object> record);

  /**
   * 插入记录
   *
   * @param modelName 模型名称
   * @param record    记录
   * @param id        auto increment name
   * @return affected rows
   */
  int insert(String modelName, Map<String, Object> record, Consumer<Object> id);

  /**
   * 插入多条记录
   *
   * @param modelName
   * @param records
   * @return
   */
  int insertAll(String modelName, List<Map<String, Object>> records);

  /**
   * 根据id获取记录
   *
   * @param modelName 模型名称
   * @param id        编号
   * @return 记录
   */
  <T> T findById(String modelName, Object id, Class<T> resultType);

  <T> List<T> find(String modelName, Query query, Class<T> resultType);

  /**
   * 根据条件统计
   *
   * @param modelName
   * @param query
   * @return 记录数
   */
  long count(String modelName, Query query);

  /**
   * 根据id更新记录
   *
   * @param modelName 模型名称
   * @param record    记录
   * @return affected rows
   */
  int updateById(String modelName, Map<String, Object> record, Object id);

  /**
   * 根据条件更新记录
   *
   * @param modelName
   * @param record
   * @param filter
   * @return 影响行数
   */
  int update(String modelName, Map<String, Object> record, String filter);

  /**
   * 根据id删除
   *
   * @param modelName 模型名称
   * @return affected rows
   */
  int deleteById(String modelName, Object id);

  /**
   * 根据条件删除
   *
   * @param modelName 模型名称
   * @param filter    条件
   * @return 影响行数
   */
  int delete(String modelName, String filter);

  /**
   * 删除所有
   *
   * @param modelName
   * @return
   */
  int deleteAll(String modelName);

  /**
   * 根据条件查询
   *
   * @param modelName
   * @param queryUnaryOperator
   * @return
   */
  default <T> List<T> find(String modelName, UnaryOperator<Query> queryUnaryOperator, Class<T> resultType) {
    Query query = new Query();
    queryUnaryOperator.apply(query);
    return find(modelName, query, resultType);
  }

  /**
   * 根据id获取数据
   *
   * @param modelName
   * @param id
   * @return
   */
  @SuppressWarnings("unchecked")
  default Map<String, Object> findById(String modelName, Object id) {
    return findById(modelName, id, Map.class);
  }

  /**
   * 根据条件查询
   *
   * @param modelName
   * @param queryUnaryOperator
   * @return
   */
  @SuppressWarnings("all")
  default List<Map<String, Object>> find(String modelName, UnaryOperator<Query> queryUnaryOperator) {
    List list = find(modelName, queryUnaryOperator, Map.class);
    return list;
  }

  default long count(String modelName, UnaryOperator<Query> queryUnaryOperator) {
    Query query = new Query();
    queryUnaryOperator.apply(query);
    return count(modelName, query);
  }

  /**
   * 根据id查询是否存在
   *
   * @param modelName
   * @param id
   * @return
   */
  default boolean existsById(String modelName, Object id) {
    return findById(modelName, id) != null;
  }

  /**
   * 查询是否存在
   *
   * @param modelName
   * @param queryUnaryOperator
   * @return
   */
  default boolean exists(String modelName, UnaryOperator<Query> queryUnaryOperator) {
    return count(modelName, queryUnaryOperator) > 0;
  }

}
