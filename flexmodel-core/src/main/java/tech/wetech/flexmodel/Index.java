package tech.wetech.flexmodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cjbi
 */
public class Index implements Serializable {

  private String name;
  private String modelName;
  private final List<Field> fields = new ArrayList<>();
  private boolean unique;

  public Index(String modelName) {
    this.modelName = modelName;
  }

  public Index(String modelName, String indexName) {
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

  public boolean isUnique() {
    return unique;
  }

  public Index setUnique(boolean unique) {
    this.unique = unique;
    return this;
  }

  public Index addField(String fieldName) {
    fields.add(new Field(fieldName, Direction.ASC));
    return this;
  }

  public Index addField(String fieldName, Direction direction) {
    fields.add(new Field(fieldName, direction));
    return this;
  }

  public String getName() {
    return name;
  }

  public Index setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (this.getName() != null && obj instanceof Index) {
      return this.getName().equals(((Index) obj).getName());
    }
    return false;
  }
}
