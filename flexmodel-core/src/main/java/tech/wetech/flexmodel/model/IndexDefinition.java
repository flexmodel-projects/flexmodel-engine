package tech.wetech.flexmodel.model;

import tech.wetech.flexmodel.query.Direction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cjbi
 */
public class IndexDefinition implements Serializable {

  private String name;
  private String modelName;
  private final List<Field> fields = new ArrayList<>();
  private boolean unique;

  public IndexDefinition(String modelName) {
    this.modelName = modelName;
  }

  public IndexDefinition(String modelName, String indexName) {
    this.modelName = modelName;
    this.name = indexName;
  }

  public record Field(String fieldName, Direction direction) implements Serializable {
  }

  public void setModelName(String modelName) {
    this.modelName = modelName;
  }

  public String getModelName() {
    return modelName;
  }

  public List<Field> getFields() {
    return fields;
  }

  public boolean containsField(String fieldName) {
    for (Field field : fields) {
      if (field.fieldName.equals(fieldName)) {
        return true;
      }
    }
    return false;
  }

  public boolean isUnique() {
    return unique;
  }

  public IndexDefinition setUnique(boolean unique) {
    this.unique = unique;
    return this;
  }

  public IndexDefinition addField(String fieldName) {
    fields.add(new Field(fieldName, Direction.ASC));
    return this;
  }

  public IndexDefinition addField(String fieldName, Direction direction) {
    fields.add(new Field(fieldName, direction));
    return this;
  }

  public String getName() {
    return name;
  }

  public IndexDefinition setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (this.getName() != null && obj instanceof IndexDefinition) {
      return this.getName().equals(((IndexDefinition) obj).getName());
    }
    return false;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "<" + getName() + ">";
  }

}
