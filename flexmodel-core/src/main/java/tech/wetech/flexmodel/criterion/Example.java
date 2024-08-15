package tech.wetech.flexmodel.criterion;

import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * @author cjbi
 */
public class Example {

  private final List<Criteria> oredCriteria;

  public Example() {
    oredCriteria = new ArrayList<>();
  }

  public List<Criteria> getOredCriteria() {
    return oredCriteria;
  }

  public Criteria createCriteria() {
    Criteria criteria = createCriteriaInternal();
    if (oredCriteria.isEmpty()) {
      oredCriteria.add(criteria);
    }
    return criteria;
  }

  public Criteria and() {
    Criteria criteria = createCriteriaInternal();
    criteria.isAnd = true;
    oredCriteria.add(criteria);
    return criteria;
  }

  public Criteria and(UnaryOperator<Criteria> and) {
    return and.apply(and());
  }

  public Criteria or() {
    Criteria criteria = createCriteriaInternal();
    criteria.isAnd = false;
    oredCriteria.add(criteria);
    return criteria;
  }

  public Criteria or(UnaryOperator<Criteria> or) {
    return or.apply(or());
  }

  protected Criteria createCriteriaInternal() {
    return new Criteria(this);
  }

  /**
   * @author cjbi
   */
  public static class Criteria {

    private boolean isAnd = true;
    private final Example that;
    private final List<Criterion> criteria;

    public Criteria(Example that) {
      this.that = that;
      this.criteria = new ArrayList<>();
    }

    private List<Criterion> getAllCriteria() {
      return criteria;
    }

    public boolean isValid() {
      return !criteria.isEmpty();
    }

    public Criteria and() {
      return that.and();
    }

    public Criteria and(UnaryOperator<Criteria> and) {
      that.and(and);
      return this;
    }

    public Criteria or() {
      return that.or();
    }

    public Criteria or(UnaryOperator<Criteria> or) {
      that.or(or);
      return this;
    }

    public Criteria equalTo(String field, Object value) {
      addCriterion(field, value, "==");
      return this;
    }

    public Criteria notEqualTo(String field, Object value) {
      addCriterion(field, value, "!=");
      return this;
    }

    public Criteria greaterThan(String field, Object value) {
      addCriterion(field, value, ">");
      return this;
    }

    public Criteria greaterThanOrEqualTo(String field, Object value) {
      addCriterion(field, value, ">=");
      return this;
    }

    public Criteria lessThan(String field, Object value) {
      addCriterion(field, value, "<");
      return this;
    }

    public Criteria lessThanOrEqualTo(String field, Object value) {
      addCriterion(field, value, "<=");
      return this;
    }

    public Criteria in(String field, Iterable<?> values) {
      addCriterion(field, values, "in");
      return this;
    }

    public Criteria notIn(String field, Iterable<?> values) {
      addCriterion(field, values, "not_in");
      return this;
    }

    public Criteria between(String field, Object value1, Object value2) {
      addCriterion(field, value1, value2, "between");
      return this;
    }

    private void addCriterion(String field, Object value1, Object value2, String operator) {
      if (field == null) {
        throw new RuntimeException("field cannot be null");
      }
      if (value1 == null || value2 == null) {
        throw new RuntimeException("Between values for " + field + " cannot be null");
      }
      criteria.add(new Criterion(field, value1, value2, operator));
    }

    private void addCriterion(String field, Object value, String operator) {
      if (field == null) {
        throw new RuntimeException("Field cannot be null");
      }
      if (value == null) {
        throw new RuntimeException("Value for " + field + " cannot be null");
      }
      criteria.add(new Criterion(field, value, operator));
    }

    private void addCriterion(String field, String operator) {
      if (field == null) {
        throw new RuntimeException("field cannot be null");
      }
      if (operator == null) {
        throw new RuntimeException("Value for operator cannot be null");
      }
      criteria.add(new Criterion(field, operator));
    }

    public void clear() {
      criteria.clear();
    }

  }

  public static class Criterion {
    private final String operator;
    private final String field;
    private Object value;
    private Object secondValue;
    private boolean noValue;
    private boolean singleValue;
    private boolean betweenValue;
    private boolean listValue;

    public Criterion(String field, String operator) {
      this.field = field;
      this.operator = operator;
      this.noValue = true;
    }

    public Criterion(String field, Object value, String operator) {
      this.field = field;
      this.value = value;
      this.operator = operator;

      if (value instanceof Iterable) {
        this.listValue = true;
      } else {
        this.singleValue = true;
      }
    }

    public Criterion(String field, Object value1, Object value2, String operator) {
      this.field = field;
      this.value = value1;
      this.secondValue = value2;
      this.operator = operator;
      this.betweenValue = true;
    }

    public String getField() {
      return field;
    }

    public Object getValue() {
      return value;
    }

    public Object getSecondValue() {
      return secondValue;
    }

    public String getOperator() {
      return operator;
    }

    public boolean isNoValue() {
      return noValue;
    }

    public boolean isSingleValue() {
      return singleValue;
    }

    public boolean isBetweenValue() {
      return betweenValue;
    }

    public boolean isListValue() {
      return listValue;
    }

  }

  public String toFilterString() {
    Map<String, Object> root = new HashMap<>();
    List<Map<String, Object>> andList = new ArrayList<>();
    root.put("and", andList);
    for (Criteria criteria : this.oredCriteria) {
      Map<String, Object> filterMap = new HashMap<>();
      List<Object> logicValues = new ArrayList<>();
      if (criteria.getAllCriteria().isEmpty()) {
        continue;
      }
      filterMap.put(criteria.isAnd ? "and" : "or", logicValues);
      for (Criterion criterion : criteria.getAllCriteria()) {
        Map<String, Object> condition = getCondition(criterion);
        logicValues.add(condition);
      }
      andList.add(filterMap);
    }
    JacksonObjectConverter converter = new JacksonObjectConverter();
    return converter.toJsonString(root);
  }

  private Map<String, Object> getCondition(Criterion criterion) {
    List<Object> item = new ArrayList<>();
    item.add(Map.of("var", List.of(criterion.getField())));
    item.add(criterion.getValue());
    if (criterion.isBetweenValue()) {
      item.add(criterion.getSecondValue());
    }
    return Map.of(criterion.getOperator(), item);
  }

}
