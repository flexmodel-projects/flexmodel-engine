package tech.wetech.flexmodel;

import java.util.*;
import java.util.function.UnaryOperator;

/**
 * @author cjbi
 */
public class Query {

  private String filter;
  private Projection projection;
  private Joins joins;
  private GroupBy groupBy;
  private Sort sort;
  private Integer limit;
  private Integer offset;

  public interface QueryCall {
  }

  public interface QueryFunc extends QueryCall {

    String operator();

    Object[] args();
  }

  public static class Projection {

    private final Map<String, QueryCall> fields = new LinkedHashMap<>();

    public Projection addField(String alias, QueryCall call) {
      this.fields.put(alias, call);
      return this;
    }

    public Map<String, QueryCall> fields() {
      return fields;
    }
  }

  public static class Joins {
    private final Map<String, Join> joinMap = new HashMap<>();

    public List<Join> joins() {
      return joinMap.values().stream().toList();
    }

    public Joins addInnerJoin(UnaryOperator<Join> joinUnaryOperator) {
      Join join = new Join();
      join.setJoinType(Join.JoinType.INNER_JOIN);
      joinUnaryOperator.apply(join);
      this.joinMap.put(join.from(), join);
      return this;
    }

    public Joins addLeftJoin(UnaryOperator<Join> joinUnaryOperator) {
      Join join = new Join();
      join.setJoinType(Join.JoinType.LEFT_JOIN);
      joinUnaryOperator.apply(join);
      this.joinMap.put(join.from(), join);
      return this;
    }

  }

  public static class GroupBy {
    private final List<QueryField> fields = new ArrayList<>();

    public GroupBy addField(String field) {
      fields.add(new QueryField(field));
      return this;
    }

    public List<QueryField> fields() {
      return fields;
    }
  }

  public Query setProjection(UnaryOperator<Projection> projectUnaryOperator) {
    Projection projection = new Projection();
    projectUnaryOperator.apply(projection);
    this.projection = projection;
    return this;
  }

  public Query setJoins(UnaryOperator<Joins> joinersUnaryOperator) {
    Joins joins = new Joins();
    joinersUnaryOperator.apply(joins);
    this.joins = joins;
    return this;
  }

  public Joins joiners() {
    return joins;
  }

  public Query setGroupBy(UnaryOperator<GroupBy> groupByUnaryOperator) {
    GroupBy groupBy = new GroupBy();
    groupByUnaryOperator.apply(groupBy);
    this.groupBy = groupBy;
    return this;
  }

  public String filter() {
    return filter;
  }

  public Query setFilter(String filter) {
    this.filter = filter;
    return this;
  }

  public Projection projection() {
    return projection;
  }

  public GroupBy groupBy() {
    return groupBy;
  }

  public Sort sort() {
    return sort;
  }

  public Query setSort(UnaryOperator<Sort> UnaryOperator) {
    this.sort = new Sort();
    UnaryOperator.apply(this.sort);
    return this;
  }

  public Integer limit() {
    return limit;
  }

  public Query setLimit(Integer limit) {
    this.limit = limit;
    return this;
  }

  public Integer offset() {
    return offset;
  }

  public Query setOffset(Integer offset) {
    this.offset = offset;
    return this;
  }

  public static class Join implements QueryCall {
    private String from;
    private String localField;
    private String foreignField;
    private String filter;
    private JoinType joinType;

    public enum JoinType {
      INNER_JOIN, LEFT_JOIN
    }

    public String from() {
      return from;
    }

    public Join setFrom(String from) {
      this.from = from;
      return this;
    }

    public String localField() {
      return localField;
    }

    public Join setLocalField(String localField) {
      this.localField = localField;
      return this;
    }

    public String foreignField() {
      return foreignField;
    }

    public Join setForeignField(String foreignField) {
      this.foreignField = foreignField;
      return this;
    }

    public String filter() {
      return filter;
    }

    public Join setFilter(String filter) {
      this.filter = filter;
      return this;
    }

    public JoinType joinType() {
      return joinType;
    }

    void setJoinType(JoinType joinType) {
      this.joinType = joinType;
    }
  }

  public static class Sort {

    private final List<Order> orders = new ArrayList<>();

    public Sort addOrder(String field, Direction direction) {
      Order order = new Order()
        .setField(field)
        .setDirection(direction);
      orders.add(order);
      return this;
    }

    public Sort addOrder(String field) {
      Order order = new Order();
      order.setField(field);
      orders.add(order);
      return this;
    }

    public List<Order> orders() {
      return orders;
    }

    public static class Order {
      private QueryField field;
      private Direction direction = Direction.ASC;

      public Order() {
      }

      public QueryField field() {
        return field;
      }

      public Order setField(String field) {
        this.field = new QueryField(field);
        return this;
      }

      public Order setDirection(Direction direction) {
        this.direction = direction;
        return this;
      }

      public Direction direction() {
        return direction;
      }

    }
  }

  public record AggFunc(String operator, QueryCall... args) implements QueryFunc {
  }

  public record QueryField(String name) implements Field, QueryCall {

    public String modelName() {
      if (name.contains(".")) {
        return name.split("\\.")[0];
      }
      return null;
    }

    public String fieldName() {
      if (name.contains(".")) {
        return name.split("\\.")[1];
      }
      return name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  public record QueryValue(Object value) implements QueryCall {

  }

  public record DateFormatFunc(QueryCall date, String fmt) implements QueryFunc {
    @Override
    public String operator() {
      return "date_format";
    }

    @Override
    public Object[] args() {
      return new Object[]{date, fmt};
    }
  }

  public record DayOfWeekFunc(Query.QueryCall date) implements QueryFunc {

    @Override
    public String operator() {
      return "dayofweek";
    }

    @Override
    public Object[] args() {
      return new Object[]{date};
    }
  }

  public record DayOfMonthFunc(Query.QueryCall date) implements QueryFunc {

    @Override
    public String operator() {
      return "dayofmonth";
    }

    @Override
    public Object[] args() {
      return new Object[]{date};
    }
  }

  public record DayOfYearFunc(Query.QueryCall date) implements QueryFunc {

    @Override
    public String operator() {
      return "dayofyear";
    }

    @Override
    public Object[] args() {
      return new Object[]{date};
    }
  }

}
