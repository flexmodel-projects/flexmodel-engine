package tech.wetech.flexmodel.core;

import tech.wetech.flexmodel.core.query.Direction;
import tech.wetech.flexmodel.core.query.Query;
import tech.wetech.flexmodel.core.query.QueryBuilder;
import tech.wetech.flexmodel.core.query.expr.Expressions;
import tech.wetech.flexmodel.core.query.expr.Predicate;

/**
 * 简化的DSL语法，提供更直观的查询接口
 *
 * @author cjbi
 */
public class QueryDSL {

  /**
   * 创建查询
   */
  public static QueryBuilder query() {
    return QueryBuilder.create();
  }

  /**
   * 创建字段引用
   */
  public static Query.QueryField field(String fieldName) {
    return new Query.QueryField(fieldName);
  }

  /**
   * 创建值引用
   */
  public static Query.QueryValue value(Object value) {
    return new Query.QueryValue(value);
  }

  /**
   * 创建条件表达式
   */
  public static FilterBuilder where(String fieldName) {
    return new FilterBuilder(fieldName);
  }

  /**
   * 创建聚合函数
   */
  public static AggregationBuilder agg() {
    return new AggregationBuilder();
  }

  /**
   * 创建日期函数
   */
  public static DateFunctionBuilder date() {
    return new DateFunctionBuilder();
  }

  /**
   * 条件构建器
   */
  public static class FilterBuilder {
    private final String fieldName;

    public FilterBuilder(String fieldName) {
      this.fieldName = fieldName;
    }

    public Predicate eq(Object value) {
      return Expressions.field(fieldName).eq(value);
    }

    public Predicate ne(Object value) {
      return Expressions.field(fieldName).ne(value);
    }

    public Predicate gt(Object value) {
      return Expressions.field(fieldName).gt(value);
    }

    public Predicate gte(Object value) {
      return Expressions.field(fieldName).gte(value);
    }

    public Predicate lt(Object value) {
      return Expressions.field(fieldName).lt(value);
    }

    public Predicate lte(Object value) {
      return Expressions.field(fieldName).lte(value);
    }

    public Predicate contains(Object value) {
      return Expressions.field(fieldName).contains(value);
    }

    public Predicate notContains(Object value) {
      return Expressions.field(fieldName).notContains(value);
    }

    public Predicate startsWith(Object value) {
      return Expressions.field(fieldName).startsWith(value);
    }

    public Predicate endsWith(Object value) {
      return Expressions.field(fieldName).endsWith(value);
    }

    public Predicate in(Object... values) {
      return Expressions.field(fieldName).in(values);
    }

    public Predicate nin(Object... values) {
      return Expressions.field(fieldName).nin(values);
    }

    public Predicate between(Object start, Object end) {
      return Expressions.field(fieldName).between(start, end);
    }
  }

  /**
   * 聚合函数构建器
   */
  public static class AggregationBuilder {

    public Query.AggFunc count(String fieldName) {
      return new Query.AggFunc("count", field(fieldName));
    }

    public Query.AggFunc sum(String fieldName) {
      return new Query.AggFunc("sum", field(fieldName));
    }

    public Query.AggFunc avg(String fieldName) {
      return new Query.AggFunc("avg", field(fieldName));
    }

    public Query.AggFunc max(String fieldName) {
      return new Query.AggFunc("max", field(fieldName));
    }

    public Query.AggFunc min(String fieldName) {
      return new Query.AggFunc("min", field(fieldName));
    }
  }

  /**
   * 日期函数构建器
   */
  public static class DateFunctionBuilder {

    public Query.DateFormatFunc format(String fieldName, String format) {
      return new Query.DateFormatFunc(field(fieldName), format);
    }

    public Query.DayOfWeekFunc dayOfWeek(String fieldName) {
      return new Query.DayOfWeekFunc(field(fieldName));
    }

    public Query.DayOfMonthFunc dayOfMonth(String fieldName) {
      return new Query.DayOfMonthFunc(field(fieldName));
    }

    public Query.DayOfYearFunc dayOfYear(String fieldName) {
      return new Query.DayOfYearFunc(field(fieldName));
    }
  }

  /**
   * 简化的查询构建器
   */
  public static class SimpleQueryBuilder {
    private final Query query = new Query();

    public SimpleQueryBuilder select(String... fields) {
      query.select(projection -> {
        for (String field : fields) {
          projection.addField(field, new Query.QueryField(field));
        }
        return projection;
      });
      return this;
    }

    public SimpleQueryBuilder select(String alias, String fieldName) {
      query.select(projection -> {
        projection.addField(alias, new Query.QueryField(fieldName));
        return projection;
      });
      return this;
    }

    public SimpleQueryBuilder where(Predicate predicate) {
      query.where(predicate);
      return this;
    }

    public SimpleQueryBuilder where(String filter) {
      query.where(filter);
      return this;
    }

    public SimpleQueryBuilder orderBy(String field, Direction direction) {
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

    public SimpleQueryBuilder limit(int pageSize) {
      query.page(1, pageSize);
      return this;
    }

    public SimpleQueryBuilder page(int pageNumber, int pageSize) {
      query.page(pageNumber, pageSize);
      return this;
    }

    public Query build() {
      return query;
    }
  }

  /**
   * 创建简单查询构建器
   */
  public static SimpleQueryBuilder simple() {
    return new SimpleQueryBuilder();
  }
}
