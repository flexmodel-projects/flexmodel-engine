package tech.wetech.flexmodel.core.model.field;

/**
 * @author cjbi
 */
public class BooleanField extends TypedField<Boolean, BooleanField> {

  public BooleanField(String name) {
    super(name, ScalarType.BOOLEAN.getType());
  }

}
