package tech.wetech.flexmodel;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;

/**
 * @author cjbi
 */
public class Entity implements Model {

  private final String name;
  private String comment;
  private final Set<TypedField<?, ?>> fields = new LinkedHashSet<>();
  private final Set<Index> indexes = new LinkedHashSet<>();

  public Entity(String name) {
    this.name = name;
  }

  @Override
  public String name() {
    return name;
  }

  public String comment() {
    return comment;
  }

  public Entity setComment(String comment) {
    this.comment = comment;
    return this;
  }

  @Override
  public List<TypedField<?, ?>> fields() {
    return fields.stream().toList();
  }

  public List<Index> indexes() {
    return indexes.stream().toList();
  }

  public Entity addField(TypedField<?, ?> field) {
    field.setModelName(name);
    fields.add(field);
    return this;
  }

  public void removeField(String fieldName) {
    fields.remove(new TypedField<>(this.name, fieldName));
  }

  public Entity addIndex(Index index) {
    indexes.add(index);
    return this;
  }

  public Entity addIndex(UnaryOperator<Index> indexUnaryOperator) {
    Index index = new Index(name);
    indexUnaryOperator.apply(index);
    indexes.add(index);
    return this;
  }

  public void removeIndex(String indexName) {
    indexes.remove(new Index(this.name, indexName));
  }

  public IDField idField() {
    return fields.stream()
      .filter(f -> f instanceof IDField)
      .map(f -> (IDField) f)
      .findFirst()
      .orElse(null);
  }

  @Override
  public boolean equals(Object obj) {
    if (this.name() != null && obj instanceof Entity) {
      return this.name().equals(((Entity) obj).name());
    }
    return false;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "<" + name() + ">";
  }

}
