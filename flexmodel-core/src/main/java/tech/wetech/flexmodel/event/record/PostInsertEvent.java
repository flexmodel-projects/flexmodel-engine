package tech.wetech.flexmodel.event.record;

import java.util.Map;

/**
 * @author cjbi
 */
public class PostInsertEvent extends AbstractRecordEvent {

  private String modelName;
  private Map<String, Object> record;
  private Object id;
  private int affectedRows;

  public PostInsertEvent() {
  }

  public PostInsertEvent(String modelName, String schemaName, Map<String, Object> record, Object id, int affectedRows) {
    this.modelName = modelName;
    this.schemaName = schemaName;
    this.record = record;
    this.id = id;
    this.affectedRows = affectedRows;
  }

  public Object getId() {
    return id;
  }

  public void setId(Object id) {
    this.id = id;
  }

  public int getAffectedRows() {
    return affectedRows;
  }

  public void setAffectedRows(int affectedRows) {
    this.affectedRows = affectedRows;
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
}
