package tech.wetech.flexmodel;

/**
 * @author cjbi
 */
public class IntField extends TypedField<Long, IntField> {
  /**
   * 自动增长
   */
  private boolean autoIncrement;

  public IntField(String name) {
    super(name, BasicFieldType.INT.getType());
  }

  public boolean isAutoIncrement() {
    return autoIncrement;
  }

  public void setAutoIncrement(boolean autoIncrement) {
    this.autoIncrement = autoIncrement;
  }
}
