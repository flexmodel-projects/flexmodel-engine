package tech.wetech.flexmodel;

/**
 * @author cjbi
 */
public class Projections {

  public static Query.QueryCall count(Query.QueryCall arg) {
    return new Query.AggFunc("count", arg);
  }

  public static Query.QueryCall avg(Query.QueryCall arg) {
    return new Query.AggFunc("avg", arg);
  }

  public static Query.QueryCall sum(Query.QueryCall arg) {
    return new Query.AggFunc("sum", arg);
  }

  public static Query.QueryCall max(Query.QueryCall arg) {
    return new Query.AggFunc("max", arg);
  }

  public static Query.QueryCall min(Query.QueryCall arg) {
    return new Query.AggFunc("min", arg);
  }

  public static Query.QueryCall dateFormat(Query.QueryCall date, String fmt) {
    return new Query.DateFormatFunc(date, fmt);
  }

  public static Query.QueryCall dayOfYear(Query.QueryCall date) {
    return new Query.DayOfYearFunc(date);
  }

  public static Query.QueryCall dayOfMonth(Query.QueryCall date) {
    return new Query.DayOfMonthFunc(date);
  }

  public static Query.QueryCall dayOfWeek(Query.QueryCall date) {
    return new Query.DayOfWeekFunc(date);
  }

  public static Query.QueryCall field(String field) {
    return new Query.QueryField(field);
  }

  public static Query.QueryCall value(Object value) {
    return new Query.QueryValue(value);
  }

}
