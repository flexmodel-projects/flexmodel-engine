package tech.wetech.flexmodel.core.query;

import tech.wetech.flexmodel.core.model.field.Field;
import tech.wetech.flexmodel.core.query.expr.Predicate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
  private Sort sort;
  private Page page;
  private boolean nestedQueryEnabled;

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

  public static class Sort implements Serializable {
    private List<Order> orders = new ArrayList<>();

    public Sort asc(String field) {
      return addOrder(field, Direction.ASC);
    }

    public Sort desc(String field) {
      return addOrder(field, Direction.DESC);
    }

    public Sort addOrder(String field, Direction direction) {
      Order order = new Order()
        .setField(field)
        .setDirection(direction);
      orders.add(order);
      return this;
    }

    public List<Order> getOrders() {
      return orders;
    }

    public static class Order implements Serializable {
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

  // 核心方法 - 统一命名
  public Query select(UnaryOperator<Projection> projection) {
    Projection proj = new Projection();
    projection.apply(proj);
    this.projection = proj;
    return this;
  }

  public Query where(Predicate condition) {
    this.filter = condition.toJsonString();
    return this;
  }

  public Query where(String filter) {
    this.filter = filter;
    return this;
  }

  public Query innerJoin(UnaryOperator<Joins> joins) {
    Joins joinList = new Joins();
    this.joins = joins.apply(joinList);
    return this;
  }

  public Query leftJoin(UnaryOperator<Joins> joins) {
    Joins joinList = new Joins();
    this.joins = joins.apply(joinList);
    return this;
  }

  public Query groupBy(UnaryOperator<GroupBy> groupBy) {
    GroupBy group = new GroupBy();
    this.groupBy = groupBy.apply(group);
    return this;
  }

  public Query orderBy(UnaryOperator<Sort> sort) {
    this.sort = sort.apply(new Sort());
    return this;
  }

  public Query page(int pageNumber, int pageSize) {
    this.page = new Page();
    page.setPageNumber(pageNumber);
    page.setPageSize(pageSize);
    return this;
  }

  public Query enableNested() {
    this.nestedQueryEnabled = true;
    return this;
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

  public Sort getSort() {
    return sort;
  }

  public Page getPage() {
    return page;
  }

  public boolean isNestedQueryEnabled() {
    return nestedQueryEnabled;
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
}
