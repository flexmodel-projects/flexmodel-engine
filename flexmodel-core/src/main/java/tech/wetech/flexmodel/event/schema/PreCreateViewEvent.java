package tech.wetech.flexmodel.event.schema;

import tech.wetech.flexmodel.Query;

/**
 * @author cjbi
 */
public class PreCreateViewEvent extends AbstractSchemaEvent {

  private String viewName;
  private String viewOn;
  private Query query;

  public PreCreateViewEvent() {
  }

  public PreCreateViewEvent(String schemaName, String viewName, String viewOn, Query query) {
    this.schemaName = schemaName;
      this.viewName = viewName;
    this.viewOn = viewOn;
    this.query = query;
  }

  public String getViewName() {
    return viewName;
  }

  public void setViewName(String viewName) {
    this.viewName = viewName;
  }

  public String getViewOn() {
    return viewOn;
  }

  public void setViewOn(String viewOn) {
    this.viewOn = viewOn;
  }

  public Query getQuery() {
    return query;
  }

  public void setQuery(Query query) {
    this.query = query;
  }
}
