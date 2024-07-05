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

  public DecimalField setPrecision(int precision) {
    this.precision = precision;
    return this;
  }

  public int getScale() {
    return scale;
  }

  public DecimalField setScale(int scale) {
    this.scale = scale;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DecimalField that)) return false;
    if (!super.equals(o)) return false;
    if (precision != that.precision) return false;
    return scale == that.scale;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + precision;
    result = 31 * result + scale;
    return result;
  }
}
