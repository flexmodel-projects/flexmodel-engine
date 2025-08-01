package tech.wetech.flexmodel.model;

import tech.wetech.flexmodel.model.field.Field;

import java.io.Serializable;
import java.util.List;

/**
 * @author cjbi
 */
public interface ModelDefinition extends Serializable, SchemaObject {

  List<? extends Field> getFields();

  default Field getField(String name) {
    return this.getFields().stream()
      .filter(f -> f.getName().equals(name))
      .findFirst()
      .orElse(null);
  }

}
