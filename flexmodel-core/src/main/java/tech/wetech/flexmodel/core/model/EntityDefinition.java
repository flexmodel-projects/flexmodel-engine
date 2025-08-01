package tech.wetech.flexmodel.core.model;

import tech.wetech.flexmodel.core.model.field.RelationField;
import tech.wetech.flexmodel.core.model.field.TypedField;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * @author cjbi
 */
public class EntityDefinition extends AbstractModelDefinition<EntityDefinition> {

  private String comment;
  private List<TypedField<?, ?>> fields = new ArrayList<>();
  private List<IndexDefinition> indexes = new ArrayList<>();

  public EntityDefinition(String name) {
    this.name = name;
  }

  @Override
  public String getType() {
    return "ENTITY";
  }

  public String getComment() {
    return comment;
  }

  public EntityDefinition setComment(String comment) {
    this.comment = comment;
    return this;
  }

  @Override
  public List<TypedField<?, ?>> getFields() {
    return fields;
  }

  public EntityDefinition setFields(List<TypedField<?, ?>> fields) {
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

  public IndexDefinition getIndex(String name) {
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

  public List<IndexDefinition> getIndexes() {
    return indexes;
  }

  public EntityDefinition setIndexes(List<IndexDefinition> indexes) {
    this.indexes = indexes;
    return this;
  }

  public EntityDefinition addField(TypedField<?, ?> field) {
    field.setModelName(name);
    fields.add(field);
    return this;
  }

  public void removeField(String fieldName) {
    fields.remove((TypedField<?, ?>) getField(fieldName));
  }

  public EntityDefinition addIndex(IndexDefinition index) {
    index.setModelName(name);
    indexes.add(index);
    return this;
  }

  public EntityDefinition addIndex(UnaryOperator<IndexDefinition> indexUnaryOperator) {
    IndexDefinition index = new IndexDefinition(name);
    indexUnaryOperator.apply(index);
    addIndex(index);
    return this;
  }

  public void removeIndex(String indexName) {
    indexes.remove(getIndex(indexName));
  }

  public Optional<TypedField<?,?>> findIdField() {
    return fields.stream()
      .filter(TypedField::isIdentity)
      .findFirst();
  }

  @Override
  public boolean equals(Object obj) {
    if (this.getName() != null && obj instanceof EntityDefinition) {
      return this.getName().equals(((EntityDefinition) obj).getName());
    }
    return false;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "<" + getName() + ">";
  }

  @Override
  public EntityDefinition clone() {
    EntityDefinition entity = new EntityDefinition(name);
    entity.setComment(comment);
    fields.forEach(entity::addField);
    indexes.forEach(entity::addIndex);
    return entity;
  }
}
