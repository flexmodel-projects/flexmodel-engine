package tech.wetech.flexmodel;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
public interface Model extends Serializable {

  Map<String, Object> getAdditionalProperties();

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
