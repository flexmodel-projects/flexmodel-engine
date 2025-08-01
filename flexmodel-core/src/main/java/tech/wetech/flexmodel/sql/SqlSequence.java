package tech.wetech.flexmodel.sql;

/**
 * @author cjbi
 */
public class SqlSequence implements Exportable {
  private String sequenceName;
  private int initialValue;
  private int incrementSize;

  public SqlSequence(String sequenceName, int initialValue, int incrementSize) {
    this.sequenceName = sequenceName;
    this.initialValue = initialValue;
    this.incrementSize = incrementSize;
  }

  public String getSequenceName() {
    return sequenceName;
  }

  public int getInitialValue() {
    return initialValue;
  }

  public int getIncrementSize() {
    return incrementSize;
  }
}
