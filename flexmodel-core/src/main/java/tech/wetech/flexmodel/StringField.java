package tech.wetech.flexmodel;

/**
 * @author cjbi
 */
public class StringField extends TypedField<String, StringField> {

  /**
   * 最大长度
   */
  private Integer length = 255;

  public StringField(String name) {
    super(name, BasicFieldType.STRING.getType());
  }

  public Integer getLength() {
    return length;
  }

  public StringField setLength(Integer max) {
    this.length = max;
    return this;
  }

}
