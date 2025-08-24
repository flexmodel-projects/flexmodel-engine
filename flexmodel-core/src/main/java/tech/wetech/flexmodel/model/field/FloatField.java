package tech.wetech.flexmodel.model.field;

/**
 * @author cjbi
 */
public class FloatField extends TypedField<Double, FloatField> {

  /**
   * 数据长度
   */
  private int precision = 20;
  /**
   * 小数长度
   */
  private int scale = 2;

  public FloatField(String name) {
    super(name, ScalarType.FLOAT.getType());
  }

  public int getPrecision() {
    return precision;
  }

  public FloatField setPrecision(int precision) {
    this.precision = precision;
    return this;
  }

  public int getScale() {
    return scale;
  }

  public FloatField setScale(int scale) {
    this.scale = scale;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof FloatField that)) return false;
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
