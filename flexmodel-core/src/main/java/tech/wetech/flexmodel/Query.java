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

    String getOperator();

    Object[] getArgs();
  }

  public static class Projection {

    private final Map<String, QueryCall> fields = new LinkedHashMap<>();

    public Projection addField(String alias, QueryCall call) {
      this.fields.put(alias, call);
      return this;
    }

    public Map<String, QueryCall> getFields() {
      return fields;
    }
  }

  public static class Joins {
    private final Map<String, Join> joinMap = new HashMap<>();

    public List<Join> getJoins() {
      return joinMap.values().stream().toList();
    }

    public Joins addInnerJoin(UnaryOperator<Join> joinUnaryOperator) {
      Join join = new Join();
      join.setJoinType(Join.JoinType.INNER_JOIN);
      joinUnaryOperator.apply(join);
      this.joinMap.put(join.getFrom(), join);
      return this;
    }

    public Joins addLeftJoin(UnaryOperator<Join> joinUnaryOperator) {
      Join join = new Join();
      join.setJoinType(Join.JoinType.LEFT_JOIN);
      joinUnaryOperator.apply(join);
      this.joinMap.put(join.getFrom(), join);
      return this;
    }

  }

  public static class GroupBy {
    private final List<QueryField> fields = new ArrayList<>();

    public GroupBy addField(String field) {
      fields.add(new QueryField(field));
      return this;
    }

    public List<QueryField> getFields() {
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

  public Joins getJoins() {
    return joins;
  }

  public Query setGroupBy(UnaryOperator<GroupBy> groupByUnaryOperator) {
    GroupBy groupBy = new GroupBy();
    groupByUnaryOperator.apply(groupBy);
    this.groupBy = groupBy;
    return this;
  }

  public String getFilter() {
    return filter;
  }

  public Query setFilter(String filter) {
    this.filter = filter;
    return this;
  }

  public Projection getProjection() {
    return projection;
  }

  public GroupBy getGroupBy() {
    return groupBy;
  }

  public Sort getSort() {
    return sort;
  }

  public Query setSort(UnaryOperator<Sort> UnaryOperator) {
    this.sort = new Sort();
    UnaryOperator.apply(this.sort);
    return this;
  }

  public Integer getLimit() {
    return limit;
  }

  public Query setLimit(Integer limit) {
    this.limit = limit;
    return this;
  }

  public Integer getOffset() {
    return offset;
  }

  public Query setOffset(Integer offset) {
    this.offset = offset;
    return this;
  }

  public static class Join implements QueryCall {
    private String from;
    /**
     * 关联字段，非必须，当存在关联关系时候，可不指定
     */
    private String localField;
    /**
     * 外键字段，非必须，当存在关联关系时，可不指定
     */
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

    public List<Order> getOrders() {
      return orders;
    }

    public static class Order {
      private QueryField field;
      private Direction direction = Direction.ASC;

      public Order() {
      }

      public QueryField getField() {
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

      public Direction getDirection() {
        return direction;
      }

    }
  }

  public record AggFunc(String getOperator, QueryCall... getArgs) implements QueryFunc {
  }

  public record QueryField(String name) implements Field, QueryCall {

    public String getModelName() {
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

}
