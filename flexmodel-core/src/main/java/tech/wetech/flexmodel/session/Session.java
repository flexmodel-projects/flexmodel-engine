package tech.wetech.flexmodel.session;

import tech.wetech.flexmodel.annotation.ModelClass;
import tech.wetech.flexmodel.operation.DataOperations;
import tech.wetech.flexmodel.operation.SchemaOperations;
import tech.wetech.flexmodel.query.Query;
import tech.wetech.flexmodel.query.expr.Predicate;

import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * @author cjbi
 */
public interface Session extends SchemaOperations, DataOperations, Closeable {

  /**
   * 开启事务
   */
  void startTransaction();

  /**
   * 提交事务
   */
  void commit();

  /**
   * 回滚事务
   */
  void rollback();

  /**
   * 关闭连接
   */
  void close();

  SessionFactory getFactory();

  String getName();

  /**
   * 获取数据操作对象
   *
   * @return 数据操作对象
   */
  DataOperations data();

  /**
   * 获取模型操作对象
   *
   * @return 模型操作对象
   */
  SchemaOperations schema();

  /**
   * 创建DSL查询构建器
   *
   * @return DSL查询构建器
   */
  default DSLQueryBuilder dsl() {
    return new DSLQueryBuilder(this);
  }

  /**
   * DSL查询构建器
   */
  class DSLQueryBuilder {
    private final Session session;
    private String modelName;
    private Class<?> entityClass;
    private final Query query;

    public DSLQueryBuilder(Session session) {
      this.session = session;
      this.query = new Query();
    }

    /**
     * 设置要查询的字段
     */
    public DSLQueryBuilder select(String... fields) {
      query.select(projection -> {
        for (String field : fields) {
          projection.addField(field, new Query.QueryField(field));
        }
        return projection;
      });
      return this;
    }

    /**
     * 设置要查询的模型名称
     */
    public DSLQueryBuilder from(String modelName) {
      this.modelName = modelName;
      return this;
    }

    /**
     * 设置要查询的实体类
     */
    public DSLQueryBuilder from(Class<?> entityClass) {
      this.entityClass = entityClass;
      this.modelName = getModelNameFromClass(entityClass);
      return this;
    }

    /**
     * 设置过滤条件
     */
    public DSLQueryBuilder where(Predicate predicate) {
      query.where(predicate);
      return this;
    }

    /**
     * 设置过滤条件（字符串形式）
     */
    public DSLQueryBuilder where(String filter) {
      query.where(filter);
      return this;
    }

    /**
     * 设置排序
     */
    public DSLQueryBuilder orderBy(String field, Direction direction) {
      query.orderBy(sort -> {
        if (direction == Direction.ASC) {
          sort.asc(field);
        } else {
          sort.desc(field);
        }
        return sort;
      });
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
     * 设置限制条数
     */
    public DSLQueryBuilder limit(int limit) {
      query.page(1, limit);
      return this;
    }

    /**
     * 设置偏移量
     */
    public DSLQueryBuilder offset(int offset) {
      // 这里需要计算页码，假设每页大小为offset+1
      int pageSize = offset + 1;
      int pageNumber = 1;
      query.page(pageNumber, pageSize);
      return this;
    }

    /**
     * 设置分组
     */
    public DSLQueryBuilder groupBy(String... fields) {
      query.groupBy(groupBy -> {
        for (String field : fields) {
          groupBy.addField(field);
        }
        return groupBy;
      });
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
     * 设置左连接
     */
    public DSLQueryBuilder leftJoin(UnaryOperator<Query.Joins> joinsUnaryOperator) {
      query.leftJoin(joinsUnaryOperator);
      return this;
    }

    /**
     * 启用嵌套查询
     */
    public DSLQueryBuilder enableNested() {
      query.enableNested();
      return this;
    }

    /**
     * 执行查询并返回指定类型的结果
     */
    public <T> List<T> execute(Class<T> resultType) {
      if (modelName == null) {
        throw new IllegalStateException("Model name or entity class must be specified using from() method");
      }
      return session.find(modelName, query, resultType);
    }

    /**
     * 执行查询并返回Map结果
     */
    @SuppressWarnings("all")
    public List<Map<String, Object>> execute() {
      List list = execute(Map.class);
      return list;
    }

    /**
     * 执行查询并返回单个结果
     */
    public <T> T executeOne(Class<T> resultType) {
      List<T> results = execute(resultType);
      return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 执行查询并返回单个Map结果
     */
    public Map<String, Object> executeOne() {
      List<Map<String, Object>> results = execute();
      return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 统计记录数
     */
    public long count() {
      if (modelName == null) {
        throw new IllegalStateException("Model name or entity class must be specified using from() method");
      }
      return session.count(modelName, query);
    }

    /**
     * 检查是否存在记录
     */
    public boolean exists() {
      return count() > 0;
    }

    /**
     * 从实体类获取模型名称
     */
    private String getModelNameFromClass(Class<?> entityClass) {
      ModelClass modelClass = entityClass.getAnnotation(ModelClass.class);
      if (modelClass != null) {
        return modelClass.value();
      }
      // 如果没有注解，使用类名作为模型名
      return entityClass.getSimpleName();
    }
  }

  /**
   * 排序方向
   */
  enum Direction {
    ASC, DESC
  }
}
