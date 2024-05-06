package tech.wetech.flexmodel;

/**
 * @author cjbi
 */
public class DecimalField extends TypedField<Number, DecimalField> {

  /**
   * 数据长度
   */
  private int precision = 20;
  /**
   * 小数长度
   */
  private int scale = 2;

  public DecimalField(String name) {
    super(name, BasicFieldType.DECIMAL.getType());
  }

  public int getPrecision() {
    return precision;
  }

  public void setPrecision(int precision) {
    this.precision = precision;
  }

  public int getScale() {
    return scale;
  }

  public void setScale(int scale) {
    this.scale = scale;
  }

}
