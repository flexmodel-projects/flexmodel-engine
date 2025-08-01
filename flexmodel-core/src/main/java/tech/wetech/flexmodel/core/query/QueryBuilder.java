package tech.wetech.flexmodel.core.query;

import tech.wetech.flexmodel.core.query.expr.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 优化的查询构建器，提供流畅的DSL语法
 *
 * @author cjbi
 */
public class QueryBuilder {

  private final Query query = new Query();

  /**
   * 创建查询构建器
   */
  public static QueryBuilder create() {
    return new QueryBuilder();
  }

  /**
   * 设置过滤条件
   */
  public QueryBuilder where(Predicate predicate) {
    query.where(predicate);
    return this;
  }

  /**
   * 设置过滤条件（字符串形式）
   */
  public QueryBuilder where(String filter) {
    query.where(filter);
    return this;
  }

  /**
   * 选择字段
   */
  public QueryBuilder select(Consumer<SelectBuilder> selectConsumer) {
    SelectBuilder selectBuilder = new SelectBuilder();
    selectConsumer.accept(selectBuilder);
    query.select(projection -> {
      selectBuilder.fields.forEach(field ->
        projection.addField(field.alias, field.expression));
      return projection;
    });
    return this;
  }

  /**
   * 选择字段（字符串形式）
   */
  public QueryBuilder select(String... fieldNames) {
    query.select(projection -> {
      for (String fieldName : fieldNames) {
        projection.addField(fieldName, new Query.QueryField(fieldName));
      }
      return projection;
    });
    return this;
  }

  /**
   * 添加内连接
   */
  public QueryBuilder innerJoin(Consumer<JoinBuilder> joinConsumer) {
    JoinBuilder joinBuilder = new JoinBuilder();
    joinConsumer.accept(joinBuilder);
    query.innerJoin(joins -> {
      joins.addInnerJoin(join -> {
        join.setFrom(joinBuilder.model);
        join.setAs(joinBuilder.alias);
        join.setLocalField(joinBuilder.localField);
        join.setForeignField(joinBuilder.foreignField);
        if (joinBuilder.filter != null) {
          join.setFilter(joinBuilder.filter);
        }
        return join;
      });
      return joins;
    });
    return this;
  }

  /**
   * 添加左连接
   */
  public QueryBuilder leftJoin(Consumer<JoinBuilder> joinConsumer) {
    JoinBuilder joinBuilder = new JoinBuilder();
    joinConsumer.accept(joinBuilder);
    query.leftJoin(joins -> {
      joins.addLeftJoin(join -> {
        join.setFrom(joinBuilder.model);
        join.setAs(joinBuilder.alias);
        join.setLocalField(joinBuilder.localField);
        join.setForeignField(joinBuilder.foreignField);
        if (joinBuilder.filter != null) {
          join.setFilter(joinBuilder.filter);
        }
        return join;
      });
      return joins;
    });
    return this;
  }

  /**
   * 分组
   */
  public QueryBuilder groupBy(Consumer<GroupByBuilder> groupByConsumer) {
    GroupByBuilder groupByBuilder = new GroupByBuilder();
    groupByConsumer.accept(groupByBuilder);
    query.groupBy(groupBy -> {
      groupByBuilder.fields.forEach(field ->
        groupBy.addField(field));
      return groupBy;
    });
    return this;
  }

  /**
   * 排序
   */
  public QueryBuilder orderBy(Consumer<OrderByBuilder> orderByConsumer) {
    OrderByBuilder orderByBuilder = new OrderByBuilder();
    orderByConsumer.accept(orderByBuilder);
    query.orderBy(sort -> {
      orderByBuilder.orders.forEach(order -> {
        if (order.direction == Direction.ASC) {
          sort.asc(order.field);
        } else {
          sort.desc(order.field);
        }
      });
      return sort;
    });
    return this;
  }

  /**
   * 分页
   */
  public QueryBuilder page(int pageNumber, int pageSize) {
    query.page(pageNumber, pageSize);
    return this;
  }

  /**
   * 启用嵌套查询
   */
  public QueryBuilder enableNested() {
    query.enableNested();
    return this;
  }

  /**
   * 构建查询对象
   */
  public Query build() {
    return query;
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
      fields.add(new SelectField(alias, QueryBuilder.count(QueryBuilder.field(fieldName))));
      return this;
    }

    public SelectBuilder sum(String fieldName) {
      return sum("sum", fieldName);
    }

    public SelectBuilder sum(String alias, String fieldName) {
      fields.add(new SelectField(alias, QueryBuilder.sum(QueryBuilder.field(fieldName))));
      return this;
    }

    public SelectBuilder avg(String fieldName) {
      return avg("avg", fieldName);
    }

    public SelectBuilder avg(String alias, String fieldName) {
      fields.add(new SelectField(alias, QueryBuilder.avg(QueryBuilder.field(fieldName))));
      return this;
    }

    public SelectBuilder max(String fieldName) {
      return max("max", fieldName);
    }

    public SelectBuilder max(String alias, String fieldName) {
      fields.add(new SelectField(alias, QueryBuilder.max(QueryBuilder.field(fieldName))));
      return this;
    }

    public SelectBuilder min(String fieldName) {
      return min("min", fieldName);
    }

    public SelectBuilder min(String alias, String fieldName) {
      fields.add(new SelectField(alias, QueryBuilder.min(QueryBuilder.field(fieldName))));
      return this;
    }

    public SelectBuilder dateFormat(String fieldName, String format) {
      return dateFormat("dateFormat", fieldName, format);
    }

    public SelectBuilder dateFormat(String alias, String fieldName, String format) {
      fields.add(new SelectField(alias, QueryBuilder.dateFormat(QueryBuilder.field(fieldName), format)));
      return this;
    }

    private static class SelectField {
      final String alias;
      final Query.QueryCall expression;

      SelectField(String alias, Query.QueryCall expression) {
        this.alias = alias;
        this.expression = expression;
      }
    }
  }

  /**
   * 连接构建器
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
