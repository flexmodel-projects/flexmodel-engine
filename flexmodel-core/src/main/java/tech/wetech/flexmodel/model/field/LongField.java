package tech.wetech.flexmodel.model.field;

/**
 * @author cjbi
 */
public class LongField extends TypedField<Long, LongField> {

  public LongField(String name) {
    super(name, ScalarType.LONG.getType());
  }

}
