package tech.wetech.flexmodel;

import java.io.Serializable;

/**
 * @author cjbi
 */
public class JSONField extends TypedField<Serializable, JSONField> {

  public JSONField(String name) {
    super(name, ScalarType.JSON.getType());
  }

}
