package tech.wetech.flexmodel;

import java.util.HashSet;
import java.util.Set;

/**
 * @author cjbi
 */
public class EnumField extends TypedField<Object, EnumField> {

  /**
   * 枚举元素
   */
  private Set<String> elements = new HashSet<>();

  /**
   * 多选
   */
  private boolean multiple;

  public EnumField(String name) {
    super(name, ScalarType.ENUM.getType());
  }

  public EnumField addElement(String element) {
    elements.add(element);
    return this;
  }

  public EnumField setElements(Set<String> elements) {
    this.elements = elements;
    return this;
  }

  public Set<String> getElements() {
    return elements;
  }

  public boolean isMultiple() {
    return multiple;
  }

  public EnumField setMultiple(boolean multiple) {
    this.multiple = multiple;
    return this;
  }

  @Override
  public String getShowType() {
    if (isMultiple()) {
      return "Enum[]";
    }
    return "Enum";
  }
}
