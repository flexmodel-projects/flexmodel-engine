package tech.wetech.flexmodel;

import java.io.Serializable;
import java.util.List;

/**
 * @author cjbi
 */
public interface Model extends Serializable {

  String getType();

  String getName();

  List<? extends Field> getFields();

  default Field getField(String name) {
    return this.getFields().stream()
      .filter(f -> f.getName().equals(name))
      .findFirst()
      .orElse(null);
  }

}
