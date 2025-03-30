package tech.wetech.flexmodel;

/**
 * @author cjbi
 */
public class StringField extends TypedField<String, StringField> {

  /**
   * 最大长度
   */
  private Integer length = 255;

  private boolean largeObject;

  public StringField(String name) {
    super(name, ScalarType.STRING.getType());
  }

  public Integer getLength() {
    return length;
  }

  public StringField setLength(Integer max) {
    this.length = max;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof StringField that)) return false;
    if (!super.equals(o)) return false;
    return getLength() != null ? getLength().equals(that.getLength()) : that.getLength() == null;
  }

  public boolean isLargeObject() {
    return largeObject;
  }

  public StringField setLargeObject(boolean largeObject) {
    this.largeObject = largeObject;
    return this;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (getLength() != null ? getLength().hashCode() : 0);
    return result;
  }

}
