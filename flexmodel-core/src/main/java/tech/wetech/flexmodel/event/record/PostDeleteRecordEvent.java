package tech.wetech.flexmodel.event.record;

/**
 * @author cjbi
 */
public class PostDeleteRecordEvent extends AbstractRecordEvent {

  private String modelName;
  private Object id;
  private String filter;
  private int affectedRows;

  public PostDeleteRecordEvent() {
  }

  public PostDeleteRecordEvent(String schemaName, String modelName, Object id, String filter, int affectedRows) {
    this.schemaName = schemaName;
    this.modelName = modelName;
    this.id = id;
    this.filter = filter;
    this.affectedRows = affectedRows;
  }

  public Object getId() {
    return id;
  }

  public void setId(Object id) {
    this.id = id;
  }

  public String getFilter() {
    return filter;
  }

  public void setFilter(String filter) {
    this.filter = filter;
  }

  public String getModelName() {
    return modelName;
  }

  public void setModelName(String modelName) {
    this.modelName = modelName;
  }

  public int getAffectedRows() {
    return affectedRows;
  }

  public void setAffectedRows(int affectedRows) {
    this.affectedRows = affectedRows;
  }
}
