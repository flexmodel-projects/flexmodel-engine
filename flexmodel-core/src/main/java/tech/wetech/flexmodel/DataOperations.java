package tech.wetech.flexmodel;

import tech.wetech.flexmodel.dsl.Predicate;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Data Operations
 *
 * @author cjbi
 */
public interface DataOperations {

  /**
   * Insert a record
   *
   * @param modelName Model name
   * @param record    Record
   * @param id        Auto increment name
   * @return Number of affected rows
   */
  int insert(String modelName, Map<String, Object> record, Consumer<Object> id);

  /**
   * Find a record by ID
   *
   * @param modelName Model name
   * @param id        ID
   * @return Record
   */
  <T> T findById(String modelName, Object id, Class<T> resultType, boolean nestedQuery);

  <T> List<T> find(String modelName, Query query, Class<T> resultType);

  <T> List<T> findByNativeQuery(String statement, Map<String, Object> params, Class<T> resultType);

  <T> List<T> findByNativeQueryModel(String modelName, Map<String, Object> params, Class<T> resultType);

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

  default int insert(String modelName, Map<String, Object> record) {
    return insert(modelName, record, id -> {
    });
  }

  /**
   * Insert multiple records
   *
   * @param modelName Model name
   * @param records   Records
   * @return Number of affected rows
   */
  default int insertAll(String modelName, List<Map<String, Object>> records) {
    int rows = -1;
    for (Map<String, Object> record : records) {
      rows += insert(modelName, record);
    }
    return rows;
  }

  /**
   * Find records based on conditions
   *
   * @param modelName          Model name
   * @param queryUnaryOperator Query unary operator
   * @return List of records
   */
  default <T> List<T> find(String modelName, UnaryOperator<Query> queryUnaryOperator, Class<T> resultType) {
    Query query = new Query();
    queryUnaryOperator.apply(query);
    return find(modelName, query, resultType);
  }

  default <T> List<T> find(String modelName, Predicate predicate, Class<T> resultType) {
    Query query = new Query();
    query.withFilter(predicate);
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
  default List<Map<String, Object>> find(String modelName, UnaryOperator<Query> queryUnaryOperator) {
    List list = find(modelName, queryUnaryOperator, Map.class);
    return list;
  }

  default List<Map<String, Object>> find(String modelName, Predicate predicate) {
    Query query = new Query();
    query.withFilter(predicate);
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
    query.withFilter(predicate);
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
  default boolean exists(String modelName, UnaryOperator<Query> queryUnaryOperator) {
    return count(modelName, queryUnaryOperator) > 0;
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
