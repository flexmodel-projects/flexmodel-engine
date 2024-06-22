package tech.wetech.flexmodel;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;

/**
 * @author cjbi
 */
public class Entity implements Model {

  private String name;
  private String comment;
  private final Set<TypedField<?, ?>> fields = new LinkedHashSet<>();
  private final Set<Index> indexes = new LinkedHashSet<>();

  public Entity(String name) {
    this.name = name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getType() {
    return "entity";
  }

  @Override
  public String getName() {
    return name;
  }

  public String getComment() {
    return comment;
  }

  public Entity setComment(String comment) {
    this.comment = comment;
    return this;
  }

  @Override
  public List<TypedField<?, ?>> getFields() {
    return fields.stream().toList();
  }

  @Override
  public TypedField<?, ?> getField(String name) {
    return this.getFields().stream()
      .filter(f -> f.getName().equals(name))
      .findFirst()
      .orElse(null);
  }

  public Index getIndex(String name) {
    return this.getIndexes().stream()
      .filter(i -> i.getName().equals(name))
      .findFirst()
      .orElse(null);
  }

  public Optional<RelationField> findRelationByEntityName(String entityName) {
    for (TypedField<?, ?> field : fields) {
      if (field instanceof RelationField relationField) {
        if (relationField.getTargetEntity().equals(entityName)) {
          return Optional.of(relationField);
        }
      }
    }
    return Optional.empty();
  }

  public List<Index> getIndexes() {
    return indexes.stream().toList();
  }

  public Entity addField(TypedField<?, ?> field) {
    field.setModelName(name);
    fields.add(field);
    return this;
  }

  public void removeField(String fieldName) {
    fields.remove((TypedField<?, ?>) getField(fieldName));
  }

  public Entity addIndex(Index index) {
    index.setModelName(name);
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
    indexes.remove(getIndex(indexName));
  }

  public Optional<IDField> findIdField() {
    return fields.stream()
      .filter(f -> f instanceof IDField)
      .map(f -> (IDField) f)
      .findFirst();
  }

  @Override
  public boolean equals(Object obj) {
    if (this.getName() != null && obj instanceof Entity) {
      return this.getName().equals(((Entity) obj).getName());
    }
    return false;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "<" + getName() + ">";
  }

}
