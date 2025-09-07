package tech.wetech.flexmodel.query;

import tech.wetech.flexmodel.model.field.Field;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * 查询对象，支持投影、连接、分组、排序、分页等功能
 *
 * @author cjbi
 */
public class Query implements Serializable {

  private String filter;
  private Projection projection;
  private Joins joins;
  private GroupBy groupBy;
  private OrderBy sort;
  private Page page;
  private boolean nestedEnabled;

  public interface QueryCall extends Serializable {
  }

  public interface QueryFunc extends QueryCall {
    String getOperator();
    Object[] getArgs();
  }

  public static class Projection implements Serializable {
    private final Map<String, QueryCall> fields = new LinkedHashMap<>();

    public Projection addField(String alias, QueryCall call) {
      this.fields.put(alias, call);
      return this;
    }

    public Map<String, QueryCall> getFields() {
      return fields;
    }
  }

  public static class Joins implements Serializable {
    private final List<Join> joins = new ArrayList<>();

    public List<Join> getJoins() {
      return joins;
    }

    public Joins addInnerJoin(UnaryOperator<Join> joinUnaryOperator) {
      Join join = new Join();
      join.setJoinType(Join.JoinType.INNER_JOIN);
      this.joins.add(joinUnaryOperator.apply(join));
      return this;
    }

    public Joins addLeftJoin(UnaryOperator<Join> joinUnaryOperator) {
      Join join = new Join();
      join.setJoinType(Join.JoinType.LEFT_JOIN);
      this.joins.add(joinUnaryOperator.apply(join));
      return this;
    }
  }

  public static class GroupBy implements Serializable {
    private final List<QueryField> fields = new ArrayList<>();

    public GroupBy addField(String field) {
      fields.add(new QueryField(field));
      return this;
    }

    public List<QueryField> getFields() {
      return fields;
    }
  }

  public static class Page implements Serializable {
    public static final int DEFAULT_PAGE_NUMBER = 1;
    public static final int DEFAULT_PAGE_SIZE = 20;

    private int pageSize = DEFAULT_PAGE_SIZE;
    private int pageNumber = DEFAULT_PAGE_NUMBER;

    public int getPageSize() {
      return pageSize;
    }

    public Page setPageSize(int pageSize) {
      this.pageSize = pageSize;
      return this;
    }

    public int getPageNumber() {
      return pageNumber;
    }

    public Page setPageNumber(int pageNumber) {
      this.pageNumber = pageNumber;
      return this;
    }

    public int getOffset() {
      return (pageNumber - 1) * pageSize;
    }
  }

  public static class Join implements QueryCall {
    private String from;
    private String as;
    private String localField;
    private String foreignField;
    private String filter;
    private JoinType joinType;

    public enum JoinType {
      INNER_JOIN, LEFT_JOIN
    }

    public String getFrom() {
      return from;
    }

    public Join setFrom(String from) {
      this.from = from;
      return this;
    }

    public String getAs() {
      if (as == null) {
        return from;
      }
      return as;
    }

    public Join setAs(String as) {
      this.as = as;
      return this;
    }

    public String getLocalField() {
      return localField;
    }

    public Join setLocalField(String localField) {
      this.localField = localField;
      return this;
    }

    public String getForeignField() {
      return foreignField;
    }

    public Join setForeignField(String foreignField) {
      this.foreignField = foreignField;
      return this;
    }

    public String getFilter() {
      return filter;
    }

    public Join withFilter(Predicate condition) {
      this.filter = condition.toJsonString();
      return this;
    }

    public Join setFilter(String filter) {
      this.filter = filter;
      return this;
    }

    public JoinType getJoinType() {
      return joinType;
    }

    void setJoinType(JoinType joinType) {
      this.joinType = joinType;
    }
  }

  public static class OrderBy implements Serializable {
    private final List<Sort> sorts = new ArrayList<>();

    public OrderBy asc(String field) {
      return addOrder(field, Direction.ASC);
    }

    public OrderBy desc(String field) {
      return addOrder(field, Direction.DESC);
    }

    public OrderBy addOrder(String field, Direction order) {
      Sort sort = new Sort()
        .setField(field)
        .setOrder(order);
      sorts.add(sort);
      return this;
    }

    public List<Sort> getSorts() {
      return sorts;
    }

    public static class Sort implements Serializable {
      private QueryField field;
      private Direction order = Direction.ASC;

      public Sort() {
      }

      public QueryField getField() {
        return field;
      }

      public Sort setField(String field) {
        this.field = new QueryField(field);
        return this;
      }

      public Sort setOrder(Direction order) {
        this.order = order;
        return this;
      }

      public Direction getDirection() {
        return order;
      }
    }
  }

  // Setter方法
  public void setFilter(String filter) {
    this.filter = filter;
  }

  public void setProjection(Projection projection) {
    this.projection = projection;
  }

  public void setJoins(Joins joins) {
    this.joins = joins;
  }

  public void setGroupBy(GroupBy groupBy) {
    this.groupBy = groupBy;
  }

  public void setOrderBy(OrderBy orderBy) {
    this.sort = orderBy;
  }

  public void setPage(Page page) {
    this.page = page;
  }

  public void setNestedEnabled(boolean nestedEnabled) {
    this.nestedEnabled = nestedEnabled;
  }

  // Getter方法
  public String getFilter() {
    return filter;
  }

  public Projection getProjection() {
    return projection;
  }

  public Joins getJoins() {
    return joins;
  }

  public GroupBy getGroupBy() {
    return groupBy;
  }

  public OrderBy getSort() {
    return sort;
  }

  public Page getPage() {
    return page;
  }

  public boolean isNestedEnabled() {
    return nestedEnabled;
  }

  // 函数和字段定义
  public record AggFunc(String getOperator, QueryCall... getArgs) implements QueryFunc {
  }

  public record QueryField(String name) implements Field, QueryCall {
    public String getAliasName() {
      if (name.contains(".")) {
        return name.split("\\.")[0];
      }
      return null;
    }

    public String getFieldName() {
      if (name.contains(".")) {
        return name.split("\\.")[1];
      }
      return name;
    }

    @Override
    public String toString() {
      return name;
    }

    @Override
    public String getName() {
      return name;
    }
  }

  public record QueryValue(Object value) implements QueryCall {
  }

  public record DateFormatFunc(QueryCall date, String fmt) implements QueryFunc {
    @Override
    public String getOperator() {
      return "date_format";
    }

    @Override
    public Object[] getArgs() {
      return new Object[]{date, fmt};
    }
  }

  public record DayOfWeekFunc(Query.QueryCall date) implements QueryFunc {
    @Override
    public String getOperator() {
      return "dayofweek";
    }

    @Override
    public Object[] getArgs() {
      return new Object[]{date};
    }
  }

  public record DayOfMonthFunc(Query.QueryCall date) implements QueryFunc {
    @Override
    public String getOperator() {
      return "dayofmonth";
    }

    @Override
    public Object[] getArgs() {
      return new Object[]{date};
    }
  }

  public record DayOfYearFunc(Query.QueryCall date) implements QueryFunc {
    @Override
    public String getOperator() {
      return "dayofyear";
    }

    @Override
    public Object[] getArgs() {
      return new Object[]{date};
    }
  }

  /**
   * 连接构建器，提供流畅的DSL语法
   */
  public static class JoinBuilder {
    private String model;
    private String alias;
    private String localField;
    private String foreignField;
    private String filter;

    public JoinBuilder model(String model) {
      this.model = model;
      return this;
    }

    public JoinBuilder as(String alias) {
      this.alias = alias;
      return this;
    }

    public JoinBuilder on(String localField, String foreignField) {
      this.localField = localField;
      this.foreignField = foreignField;
      return this;
    }

    public JoinBuilder where(String filter) {
      this.filter = filter;
      return this;
    }

    public JoinBuilder where(Predicate predicate) {
      this.filter = predicate.toJsonString();
      return this;
    }
  }

  /**
   * 查询构建器，提供流畅的DSL语法
   */
  public static class Builder {
    private final Query query = new Query();

    /**
     * 创建查询构建器
     */
    public static Builder create() {
      return new Builder();
    }

    /**
     * 设置过滤条件
     */
    public Builder where(Predicate predicate) {
      query.setFilter(predicate.toJsonString());
      return this;
    }

    /**
     * 设置过滤条件（字符串形式）
     */
    public Builder where(String filter) {
      query.setFilter(filter);
      return this;
    }

    /**
     * 选择字段
     */
    public Builder select(Consumer<SelectBuilder> selectConsumer) {
      SelectBuilder selectBuilder = new SelectBuilder();
      selectConsumer.accept(selectBuilder);
      Projection projection = query.getProjection();
      if (projection == null) {
        projection = new Projection();
        query.setProjection(projection);
      }
      final Projection finalProjection = projection;
      selectBuilder.fields.forEach(field ->
        finalProjection.addField(field.alias, field.expression));
      return this;
    }

    /**
     * 选择字段（字符串形式）
     */
    public Builder select(String... fieldNames) {
      Projection projection = query.getProjection();
      if (projection == null) {
        projection = new Projection();
        query.setProjection(projection);
      }
      for (String fieldName : fieldNames) {
        projection.addField(fieldName, new Query.QueryField(fieldName));
      }
      return this;
    }

    /**
     * 添加内连接
     */
    public Builder innerJoin(Consumer<JoinBuilder> joinConsumer) {
      JoinBuilder joinBuilder = new JoinBuilder();
      joinConsumer.accept(joinBuilder);

      if (query.getJoins() == null) {
        query.setJoins(new Joins());
      }

      query.getJoins().addInnerJoin(join -> {
        join.setFrom(joinBuilder.model);
        join.setAs(joinBuilder.alias);
        join.setLocalField(joinBuilder.localField);
        join.setForeignField(joinBuilder.foreignField);
        if (joinBuilder.filter != null) {
          join.setFilter(joinBuilder.filter);
        }
        return join;
      });
      return this;
    }

    /**
     * 添加左连接
     */
    public Builder leftJoin(Consumer<JoinBuilder> joinConsumer) {
      JoinBuilder joinBuilder = new JoinBuilder();
      joinConsumer.accept(joinBuilder);

      if (query.getJoins() == null) {
        query.setJoins(new Joins());
      }

      query.getJoins().addLeftJoin(join -> {
        join.setFrom(joinBuilder.model);
        join.setAs(joinBuilder.alias);
        join.setLocalField(joinBuilder.localField);
        join.setForeignField(joinBuilder.foreignField);
        if (joinBuilder.filter != null) {
          join.setFilter(joinBuilder.filter);
        }
        return join;
      });
      return this;
    }

    /**
     * 分组
     */
    public Builder groupBy(Consumer<GroupByBuilder> groupByConsumer) {
      GroupByBuilder groupByBuilder = new GroupByBuilder();
      groupByConsumer.accept(groupByBuilder);
      GroupBy groupBy = new GroupBy();
      groupByBuilder.fields.forEach(field ->
        groupBy.addField(field));
      query.setGroupBy(groupBy);
      return this;
    }

    /**
     * 排序
     */
    public Builder orderBy(Consumer<OrderByBuilder> orderByConsumer) {
      OrderByBuilder orderByBuilder = new OrderByBuilder();
      orderByConsumer.accept(orderByBuilder);
      OrderBy sort = new OrderBy();
      orderByBuilder.orders.forEach(order -> {
        if (order.direction == Direction.ASC) {
          sort.asc(order.field);
        } else {
          sort.desc(order.field);
        }
      });
      query.setOrderBy(sort);
      return this;
    }

    /**
     * 分页
     */
    public Builder page(int pageNumber, int pageSize) {
      Page page = new Page();
      page.setPageNumber(pageNumber);
      page.setPageSize(pageSize);
      query.setPage(page);
      return this;
    }

    /**
     * 启用嵌套查询
     */
    public Builder enableNested() {
      query.setNestedEnabled(true);
      return this;
    }

    /**
     * 构建查询对象
     */
    public Query build() {
      return query;
    }
  }

  /**
   * 字段选择构建器
   */
  public static class SelectBuilder {
    private final List<SelectField> fields = new ArrayList<>();

    public SelectBuilder field(String fieldName) {
      return field(fieldName, fieldName);
    }

    public SelectBuilder field(String alias, String fieldName) {
      fields.add(new SelectField(alias, new Query.QueryField(fieldName)));
      return this;
    }

    public SelectBuilder field(String alias, Query.QueryCall expression) {
      fields.add(new SelectField(alias, expression));
      return this;
    }

    public SelectBuilder count(String fieldName) {
      return count("count", fieldName);
    }

    public SelectBuilder count(String alias, String fieldName) {
      fields.add(new SelectField(alias, Query.count(Query.field(fieldName))));
      return this;
    }

    public SelectBuilder sum(String fieldName) {
      return sum("sum", fieldName);
    }

    public SelectBuilder sum(String alias, String fieldName) {
      fields.add(new SelectField(alias, Query.sum(Query.field(fieldName))));
      return this;
    }

    public SelectBuilder avg(String fieldName) {
      return avg("avg", fieldName);
    }

    public SelectBuilder avg(String alias, String fieldName) {
      fields.add(new SelectField(alias, Query.avg(Query.field(fieldName))));
      return this;
    }

    public SelectBuilder max(String fieldName) {
      return max("max", fieldName);
    }

    public SelectBuilder max(String alias, String fieldName) {
      fields.add(new SelectField(alias, Query.max(Query.field(fieldName))));
      return this;
    }

    public SelectBuilder min(String fieldName) {
      return min("min", fieldName);
    }

    public SelectBuilder min(String alias, String fieldName) {
      fields.add(new SelectField(alias, Query.min(Query.field(fieldName))));
      return this;
    }

    public SelectBuilder dateFormat(String fieldName, String format) {
      return dateFormat("dateFormat", fieldName, format);
    }

    public SelectBuilder dateFormat(String alias, String fieldName, String format) {
      fields.add(new SelectField(alias, Query.dateFormat(Query.field(fieldName), format)));
      return this;
    }

    public static class SelectField {
      final String alias;
      final Query.QueryCall expression;

      SelectField(String alias, Query.QueryCall expression) {
        this.alias = alias;
        this.expression = expression;
      }
    }

    public List<SelectField> getFields() {
      return new ArrayList<>(fields);
    }
  }

  /**
   * 分组构建器
   */
  public static class GroupByBuilder {
    private final List<String> fields = new ArrayList<>();

    public GroupByBuilder field(String fieldName) {
      fields.add(fieldName);
      return this;
    }
  }

  /**
   * 排序构建器
   */
  public static class OrderByBuilder {
    private final List<OrderField> orders = new ArrayList<>();

    public OrderByBuilder asc(String fieldName) {
      orders.add(new OrderField(fieldName, Direction.ASC));
      return this;
    }

    public OrderByBuilder desc(String fieldName) {
      orders.add(new OrderField(fieldName, Direction.DESC));
      return this;
    }

    private static class OrderField {
      final String field;
      final Direction direction;

      OrderField(String field, Direction direction) {
        this.field = field;
        this.direction = direction;
      }
    }
  }

  // 静态辅助方法，提供更简洁的语法
  public static Query.QueryField field(String fieldName) {
    return new Query.QueryField(fieldName);
  }

  public static Query.QueryValue value(Object value) {
    return new Query.QueryValue(value);
  }

  public static Query.AggFunc count(Query.QueryCall field) {
    return new Query.AggFunc("count", field);
  }

  public static Query.AggFunc sum(Query.QueryCall field) {
    return new Query.AggFunc("sum", field);
  }

  public static Query.AggFunc avg(Query.QueryCall field) {
    return new Query.AggFunc("avg", field);
  }

  public static Query.AggFunc max(Query.QueryCall field) {
    return new Query.AggFunc("max", field);
  }

  public static Query.AggFunc min(Query.QueryCall field) {
    return new Query.AggFunc("min", field);
  }

  public static Query.DateFormatFunc dateFormat(Query.QueryCall date, String format) {
    return new Query.DateFormatFunc(date, format);
  }

  public static Query.DayOfWeekFunc dayOfWeek(Query.QueryCall date) {
    return new Query.DayOfWeekFunc(date);
  }

  public static Query.DayOfMonthFunc dayOfMonth(Query.QueryCall date) {
    return new Query.DayOfMonthFunc(date);
  }

  public static Query.DayOfYearFunc dayOfYear(Query.QueryCall date) {
    return new Query.DayOfYearFunc(date);
  }
}
