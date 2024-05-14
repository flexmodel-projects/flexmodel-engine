package tech.wetech.flexmodel;

/**
 * @author cjbi
 */
public class IntField extends TypedField<Long, IntField> {

  public IntField(String name) {
    super(name, BasicFieldType.INT.getType());
  }

}
