package tech.wetech.flexmodel.model.field;

/**
 * @author cjbi
 */
public class IntField extends TypedField<Integer, IntField> {

  public IntField(String name) {
    super(name, ScalarType.INT.getType());
  }

}
