package tech.wetech.flexmodel.core.operation;

import tech.wetech.flexmodel.core.query.Query;
import tech.wetech.flexmodel.core.query.expr.Predicate;

import java.util.List;
import java.util.Map;
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
   * @param record    Record (id should be included in the record map if needed)
   * @return Number of affected rows
   */
  int insert(String modelName, Object record);

  /**
   * Find a record by ID
   *
   * @param modelName Model name
   * @param id        ID
   * @return Record
   */
  <T> T findById(String modelName, Object id, Class<T> resultType, boolean nestedQuery);

  <T> List<T> find(String modelName, Query query, Class<T> resultType);

  <T> List<T> findByNativeQueryStatement(String statement, Object params, Class<T> resultType);

  <T> List<T> findByNativeQueryModel(String modelName, Object params, Class<T> resultType);

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
  int updateById(String modelName, Object record, Object id);

  /**
   * Update records based on conditions
   *
   * @param modelName Model name
   * @param record    Record
   * @param filter    Filter
   * @return Number of affected rows
   */
  int update(String modelName, Object record, String filter);

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
  /**
   * Insert multiple records
   *
   * @param modelName Model name
   * @param records   Records
   * @return Number of affected rows
   */
  @SuppressWarnings("all")
  default int insertAll(String modelName, List records) {
    int rows = 0;
    for (Object record : records) {
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
    query.where(predicate);
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
    query.where(predicate);
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
    query.where(predicate);
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
  default int update(String modelName, Object record, Predicate predicate) {
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

  // ========== DSL 链式调用方法 ==========

  /**
   * 创建 DSL 查询构建器
   *
   * @param modelName 模型名称
   * @return DSL 查询构建器
   */
  default DSLQueryBuilder query(String modelName) {
    return new DSLQueryBuilder(this, modelName);
  }

  /**
   * DSL 查询构建器
   */
  class DSLQueryBuilder {
    private final DataOperations dataOperations;
    private final String modelName;
    private final Query query;

    public DSLQueryBuilder(DataOperations dataOperations, String modelName) {
      this.dataOperations = dataOperations;
      this.modelName = modelName;
      this.query = new Query();
    }

    /**
     * 设置过滤条件
     */
    public DSLQueryBuilder where(Predicate predicate) {
      query.where(predicate);
      return this;
    }

    /**
     * 设置过滤条件（使用 UnaryOperator）
     */
    public DSLQueryBuilder where(UnaryOperator<Query> queryUnaryOperator) {
      queryUnaryOperator.apply(query);
      return this;
    }

    /**
     * 设置投影
     */
    public DSLQueryBuilder select(UnaryOperator<Query.Projection> projectionUnaryOperator) {
      query.select(projectionUnaryOperator);
      return this;
    }

    /**
     * 设置排序
     */
    public DSLQueryBuilder orderBy(UnaryOperator<Query.Sort> sortUnaryOperator) {
      query.orderBy(sortUnaryOperator);
      return this;
    }

    /**
     * 设置分页
     */
    public DSLQueryBuilder page(int pageNumber, int pageSize) {
      query.page(pageNumber, pageSize);
      return this;
    }

    /**
     * 设置分组
     */
    public DSLQueryBuilder groupBy(UnaryOperator<Query.GroupBy> groupByUnaryOperator) {
      query.groupBy(groupByUnaryOperator);
      return this;
    }

    /**
     * 设置连接
     */
    public DSLQueryBuilder join(UnaryOperator<Query.Joins> joinsUnaryOperator) {
      query.innerJoin(joinsUnaryOperator);
      return this;
    }

    /**
     * 启用嵌套查询
     */
    public DSLQueryBuilder enableNestedQuery() {
      query.enableNested();
      return this;
    }

    /**
     * 执行查询并返回指定类型的结果
     */
    public <T> List<T> find(Class<T> resultType) {
      return dataOperations.find(modelName, query, resultType);
    }

    /**
     * 执行查询并返回 Map 结果
     */
    public List<Map<String, Object>> find() {
      return dataOperations.find(modelName, query);
    }

    /**
     * 执行查询并返回单个结果
     */
    public <T> T findOne(Class<T> resultType) {
      List<T> results = find(resultType);
      return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 执行查询并返回单个 Map 结果
     */
    public Map<String, Object> findOne() {
      List<Map<String, Object>> results = find();
      return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 统计记录数
     */
    public long count() {
      return dataOperations.count(modelName, query);
    }

    /**
     * 检查是否存在记录
     */
    public boolean exists() {
      return count() > 0;
    }
  }

  /**
   * 创建 DSL 更新构建器
   */
  default DSLUpdateBuilder update(String modelName) {
    return new DSLUpdateBuilder(this, modelName);
  }

  /**
   * DSL 更新构建器
   */
  class DSLUpdateBuilder {
    private final DataOperations dataOperations;
    private final String modelName;
    private Object record;
    private Predicate predicate;

    public DSLUpdateBuilder(DataOperations dataOperations, String modelName) {
      this.dataOperations = dataOperations;
      this.modelName = modelName;
    }

    /**
     * 设置要更新的记录
     */
    public DSLUpdateBuilder set(Object record) {
      this.record = record;
      return this;
    }

    /**
     * 设置更新条件
     */
    public DSLUpdateBuilder where(Predicate predicate) {
      this.predicate = predicate;
      return this;
    }

    /**
     * 执行更新
     */
    public int execute() {
      if (predicate != null) {
        return dataOperations.update(modelName, record, predicate);
      } else {
        throw new IllegalStateException("Update condition is required");
      }
    }
  }

  /**
   * 创建 DSL 删除构建器
   */
  default DSLDeleteBuilder delete(String modelName) {
    return new DSLDeleteBuilder(this, modelName);
  }

  /**
   * DSL 删除构建器
   */
  class DSLDeleteBuilder {
    private final DataOperations dataOperations;
    private final String modelName;
    private Predicate predicate;

    public DSLDeleteBuilder(DataOperations dataOperations, String modelName) {
      this.dataOperations = dataOperations;
      this.modelName = modelName;
    }

    /**
     * 设置删除条件
     */
    public DSLDeleteBuilder where(Predicate predicate) {
      this.predicate = predicate;
      return this;
    }

    /**
     * 执行删除
     */
    public int execute() {
      if (predicate != null) {
        return dataOperations.delete(modelName, predicate);
      } else {
        return dataOperations.deleteAll(modelName);
      }
    }
  }

}
