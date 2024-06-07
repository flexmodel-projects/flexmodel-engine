package tech.wetech.flexmodel.event.record;

import java.util.Map;

/**
 * @author cjbi
 */
public class PreUpdateEvent extends AbstractRecordEvent {

  private String modelName;
  private String filter;
  private Object id;
  private Map<String, Object> record;

  public PreUpdateEvent() {
  }

  public PreUpdateEvent(String schemaName, String modelName, Map<String, Object> record, Object id, String filter) {
    this.schemaName = schemaName;
    this.modelName = modelName;
    this.record = record;
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

  public Map<String, Object> getRecord() {
    return record;
  }

  public void setRecord(Map<String, Object> record) {
    this.record = record;
  }

  public String getFilter() {
    return filter;
  }

  public void setFilter(String filter) {
    this.filter = filter;
  }
}
