package tech.wetech.flexmodel.event.record;

/**
 * @author cjbi
 */
public class PreDeleteEvent extends AbstractRecordEvent {

  private String modelName;
  private String filter;
  private Object id;

  public PreDeleteEvent() {
  }

  public PreDeleteEvent(String schemaName, String modelName, Object id, String filter) {
    this.schemaName = schemaName;
    this.modelName = modelName;
    this.id = id;
    this.filter = filter;
  }

  public Object getId() {
    return id;
  }

  public void setId(Object id) {
    this.id = id;
  }

  public String getModelName() {
    return modelName;
  }

  public void setModelName(String modelName) {
    this.modelName = modelName;
  }

  public String getFilter() {
    return filter;
  }

  public void setFilter(String filter) {
    this.filter = filter;
  }
}
