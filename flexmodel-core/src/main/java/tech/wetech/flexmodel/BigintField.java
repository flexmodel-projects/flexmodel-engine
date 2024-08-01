package tech.wetech.flexmodel;

/**
 * @author cjbi
 */
public class BigintField extends TypedField<Long, BigintField> {

  public BigintField(String name) {
    super(name, ScalarType.BIGINT.getType());
  }

}
