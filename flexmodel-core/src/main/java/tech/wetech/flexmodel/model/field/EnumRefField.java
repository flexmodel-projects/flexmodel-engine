package tech.wetech.flexmodel.model.field;

import java.io.Serializable;

/**
 * @author cjbi
 */
public class EnumRefField extends TypedField<Serializable, EnumRefField> {

  /**
   * 多选
   */
  private boolean multiple;

  /**
   * 从枚举定义中获取
   */
  private String from;

  public EnumRefField(String name) {
    super(name, ScalarType.ENUM.getType());
  }

  public boolean isMultiple() {
    return multiple;
  }

  public EnumRefField setMultiple(boolean multiple) {
    this.multiple = multiple;
    return this;
  }

  @Override
  public String getConcreteType() {
    if (isMultiple()) {
      return from + "[]";
    }
    return from;
  }

  public String getFrom() {
    return from;
  }

  public EnumRefField setFrom(String from) {
    this.from = from;
    return this;
  }
}
