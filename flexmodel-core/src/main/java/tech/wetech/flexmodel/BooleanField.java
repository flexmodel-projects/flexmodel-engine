package tech.wetech.flexmodel;

/**
 * @author cjbi
 */
public class BooleanField extends TypedField<Boolean, BooleanField> {

  public BooleanField(String name) {
    super(name, BasicFieldType.BOOLEAN.getType());
  }

}
