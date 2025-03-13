package tech.wetech.flexmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * @author cjbi
 */
public class Entity extends AbstractModel<Entity> {

  private String comment;
  private List<TypedField<?, ?>> fields = new ArrayList<>();
  private List<Index> indexes = new ArrayList<>();

  public Entity(String name) {
    this.name = name;
  }

  @Override
  public String getType() {
    return "ENTITY";
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
    return fields;
  }

  public Entity setFields(List<TypedField<?, ?>> fields) {
    this.fields = fields;
    return this;
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

  public Optional<RelationField> findRelationByModelName(String modelName) {
    for (TypedField<?, ?> field : fields) {
      if (field instanceof RelationField relationField) {
        if (relationField.getFrom().equals(modelName)) {
          return Optional.of(relationField);
        }
      }
    }
    return Optional.empty();
  }

  public List<Index> getIndexes() {
    return indexes;
  }

  public Entity setIndexes(List<Index> indexes) {
    this.indexes = indexes;
    return this;
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
    addIndex(index);
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

  @Override
  public Entity clone() {
    Entity entity = new Entity(name);
    entity.setComment(comment);
    fields.forEach(entity::addField);
    indexes.forEach(entity::addIndex);
    return entity;
  }
}
