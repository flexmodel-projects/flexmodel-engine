package tech.wetech.flexmodel.event.schema;

/**
 * @author cjbi
 */
public class PreCreateSequenceEvent extends AbstractSchemaEvent {

  private String sequenceName;
  private int initialValue;
  private int incrementSize;

  public PreCreateSequenceEvent() {
  }

  public PreCreateSequenceEvent(String schemaName, String sequenceName, int initialValue, int incrementSize) {
    this.schemaName = schemaName;
    this.sequenceName = sequenceName;
    this.initialValue = initialValue;
    this.incrementSize = incrementSize;
  }

  public String getSequenceName() {
    return sequenceName;
  }

  public void setSequenceName(String sequenceName) {
    this.sequenceName = sequenceName;
  }

  public int getInitialValue() {
    return initialValue;
  }

  public void setInitialValue(int initialValue) {
    this.initialValue = initialValue;
  }

  public int getIncrementSize() {
    return incrementSize;
  }

  public void setIncrementSize(int incrementSize) {
    this.incrementSize = incrementSize;
  }
}
