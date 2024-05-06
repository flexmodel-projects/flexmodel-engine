package tech.wetech.flexmodel;

import java.io.Serializable;

/**
 * @author cjbi
 */
public class JsonField extends TypedField<Serializable, JsonField> {

  public JsonField(String name) {
    super(name, BasicFieldType.JSON.getType());
  }

}
