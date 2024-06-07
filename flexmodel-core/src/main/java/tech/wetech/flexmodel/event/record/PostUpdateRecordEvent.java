package tech.wetech.flexmodel.event.record;

import java.util.Map;

/**
 * @author cjbi
 */
public class PostUpdateRecordEvent extends AbstractRecordEvent {

  private String modelName;
  private Object id;
  private String filter;
  private Map<String, Object> record;
  private int affectedRows;

  public PostUpdateRecordEvent() {
  }

  public PostUpdateRecordEvent(String schemaName, String modelName, Map<String, Object> record, Object id, String filter, int affectedRows) {
    this.schemaName = schemaName;
    this.modelName = modelName;
    this.record = record;
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

  public Map<String, Object> getRecord() {
    return record;
  }

  public void setRecord(Map<String, Object> record) {
    this.record = record;
  }

  public int getAffectedRows() {
    return affectedRows;
  }

  public void setAffectedRows(int affectedRows) {
    this.affectedRows = affectedRows;
  }
}
