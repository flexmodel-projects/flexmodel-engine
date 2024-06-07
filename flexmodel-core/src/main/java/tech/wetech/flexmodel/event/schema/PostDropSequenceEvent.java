package tech.wetech.flexmodel.event.schema;

/**
 * @author cjbi
 */
public class PostDropSequenceEvent extends AbstractSchemaEvent {

  private String sequenceName;

  public PostDropSequenceEvent() {
  }

  public PostDropSequenceEvent(String schemaName, String sequenceName) {
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
