package tech.wetech.flexmodel;

/**
 * @author cjbi
 */
public class TextField extends TypedField<String, TextField> {

  public TextField(String name) {
    super(name, ScalarType.TEXT.getType());
  }

}
