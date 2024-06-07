package tech.wetech.flexmodel.event.schema;

/**
 * @author cjbi
 */
public class PreDropSequenceEvent extends AbstractSchemaEvent {

  private String sequenceName;

  public PreDropSequenceEvent() {
  }

  public PreDropSequenceEvent(String schemaName, String sequenceName) {
    this.schemaName = schemaName;
    this.sequenceName = sequenceName;
  }

  public String getSequenceName() {
    return sequenceName;
  }

  public void setSequenceName(String sequenceName) {
    this.sequenceName = sequenceName;
  }

}
