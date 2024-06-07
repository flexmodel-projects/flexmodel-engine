package tech.wetech.flexmodel.event.record;

import java.util.Map;

/**
 * @author cjbi
 */
public class PreInsertEvent extends AbstractRecordEvent {

  private String modelName;
  private Map<String, Object> record;

  public PreInsertEvent() {
  }

  public PreInsertEvent(String schemaName, String modelName, Map<String, Object> record) {
    this.schemaName = schemaName;
    this.modelName = modelName;
    this.record = record;
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
