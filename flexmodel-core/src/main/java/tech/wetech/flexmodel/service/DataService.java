package tech.wetech.flexmodel.service;

import tech.wetech.flexmodel.query.Predicate;
import tech.wetech.flexmodel.query.Query;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * 数据服务
 *
 * @author cjbi
 */
public interface DataService {

  /**
   * Insert a record
   *
   * @param modelName Model name
   * @param record    Record (id should be included in the record map if needed)
   * @return Number of affected rows
   */
  int insert(String modelName, Map<String, Object> record);

  /**
   * Find a record by ID
   *
   * @param modelName Model name
   * @param id        ID
   * @return Record
   */
  <T> T findById(String modelName, Object id, Class<T> resultType, boolean nestedQuery);

  <T> List<T> find(String modelName, Query query, Class<T> resultType);

  <T> List<T> findByNativeQuery(String modelName, Object params, Class<T> resultType);

  /**
   * 执行原生 SQL 语句（自动判断查询或更新操作）
   *
   * @param statement 原生 SQL 语句，支持 ${paramName} 占位符
   * @param params    参数对象
   * @return 查询操作返回 List<Map<String, Object>>，更新操作返回 Integer（影响行数）
   */
  Object executeNativeStatement(String statement, Object params);

  /**
   * Count records based on conditions
   *
   * @param modelName Model name
   * @param query     Query
   * @return Number of records
   */
  long count(String modelName, Query query);

  /**
   * Update a record by ID
   *
   * @param modelName Model name
   * @param record    Record
   * @param id        ID
   * @return Number of affected rows
   */
  int updateById(String modelName, Map<String, Object> record, Object id);

  /**
   * Update records based on conditions
   *
   * @param modelName Model name
   * @param record    Record
   * @param filter    Filter
   * @return Number of affected rows
   */
  int update(String modelName, Map<String, Object> record, String filter);

  /**
   * Delete a record by ID
   *
   * @param modelName Model name
   * @param id        ID
   * @return Number of affected rows
   */
  int deleteById(String modelName, Object id);

  /**
   * Delete records based on conditions
   *
   * @param modelName Model name
   * @param filter    Filter
   * @return Number of affected rows
   */
  int delete(String modelName, String filter);

  /**
   * Delete all records
   *
   * @param modelName Model name
   * @return Number of affected rows
   */
  int deleteAll(String modelName);

  /**
   * Insert multiple records
   *
   * @param modelName Model name
   * @param records   Records
   * @return Number of affected rows
   */
  @SuppressWarnings("all")
  default int insertAll(String modelName, List<Map<String, Object>> records) {
    int rows = 0;
    for (Map<String, Object> record : records) {
      rows += insert(modelName, record);
    }
    return rows;
  }

  default <T> List<T> find(String modelName, Predicate predicate, Class<T> resultType) {
    Query query = new Query();
    query.setFilter(predicate.toJsonString());
    return find(modelName, query, resultType);
  }

  /**
   * Find a record by ID
   *
   * @param modelName  Model name
   * @param id         ID
   * @param resultType Result type
   * @param <T>        Type parameter
   * @return Record
   */
  default <T> T findById(String modelName, Object id, Class<T> resultType) {
    return findById(modelName, id, resultType, false);
  }

  /**
   * Find a record by ID
   *
   * @param modelName Model name
   * @param id        ID
   * @return Record
   */
  @SuppressWarnings("unchecked")
  default Map<String, Object> findById(String modelName, Object id) {
    return findById(modelName, id, Map.class);
  }

  /**
   * Find a record by ID
   *
   * @param modelName   Model name
   * @param id          ID
   * @param nestedQuery Whether to perform a nested fetch
   * @return Record
   */
  @SuppressWarnings("unchecked")
  default Map<String, Object> findById(String modelName, Object id, boolean nestedQuery) {
    return findById(modelName, id, Map.class, nestedQuery);
  }

  /**
   * Find records based on conditions
   *
   * @param modelName          Model name
   * @param queryUnaryOperator Query unary operator
   * @return List of records
   */
  @SuppressWarnings("all")
  default List<Map<String, Object>> find(String modelName, UnaryOperator<Query.Builder> queryUnaryOperator) {
    List list = find(modelName, queryUnaryOperator.apply(Query.Builder.create()).build(), Map.class);
    return list;
  }

  default List<Map<String, Object>> find(String modelName, Predicate predicate) {
    Query query = new Query();
    query.setFilter(predicate.toJsonString());
    List list = find(modelName, query, Map.class);
    return list;
  }

  @SuppressWarnings("all")
  default List<Map<String, Object>> find(String modelName, Query query) {
    List list = find(modelName, query, Map.class);
    return list;
  }

  default long count(String modelName, UnaryOperator<Query> queryUnaryOperator) {
    Query query = new Query();
    queryUnaryOperator.apply(query);
    return count(modelName, query);
  }

  default long count(String modelName, Predicate predicate) {
    Query query = new Query();
    query.setFilter(predicate.toJsonString());
    return count(modelName, query);
  }

  /**
   * Check if a record exists by ID
   *
   * @param modelName Model name
   * @param id        ID
   * @return True if exists, false otherwise
   */
  default boolean existsById(String modelName, Object id) {
    return findById(modelName, id) != null;
  }

  /**
   * Check if records exist based on conditions
   *
   * @param modelName          Model name
   * @param queryUnaryOperator Query unary operator
   * @return True if exists, false otherwise
   */
  default boolean exists(String modelName, UnaryOperator<Query.Builder> queryUnaryOperator) {
    return count(modelName, queryUnaryOperator.apply(Query.Builder.create()).build()) > 0;
  }

  /**
   * Update records based on conditions
   *
   * @param modelName Model name
   * @param record    Record
   * @param predicate predicate
   * @return Number of affected rows
   */
  default int update(String modelName, Map<String, Object> record, Predicate predicate) {
    return update(modelName, record, predicate.toJsonString());
  }

  /**
   * Delete records based on conditions
   *
   * @param modelName Model name
   * @param predicate Unary operator for criteria
   * @return Number of affected rows
   */
  default int delete(String modelName, Predicate predicate) {
    return delete(modelName, predicate.toJsonString());
  }

}
