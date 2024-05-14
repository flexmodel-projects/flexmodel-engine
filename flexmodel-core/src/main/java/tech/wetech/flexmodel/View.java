package tech.wetech.flexmodel;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author cjbi
 */
public class View implements Model {

  private final String name;
  private String viewOn;
  private Query query;

  public View(String name) {
    this.name = name;
  }

  public String getViewOn() {
    return viewOn;
  }

  public View setViewOn(String viewOn) {
    this.viewOn = viewOn;
    return this;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public List<? extends Field> getFields() {
    Map<String, Query.QueryCall> fields = this.getQuery().getProjection().getFields();
    return fields.keySet()
      .stream()
      .map(Query.QueryField::new)
      .collect(Collectors.toList());
  }

  public Query getQuery() {
    return query;
  }

  public View setQuery(Query query) {
    this.query = query;
    return this;
  }
}
