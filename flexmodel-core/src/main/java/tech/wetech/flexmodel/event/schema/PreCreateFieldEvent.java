package tech.wetech.flexmodel.event.schema;

import tech.wetech.flexmodel.Entity;
import tech.wetech.flexmodel.TypedField;

/**
 * @author cjbi
 */
public class PreCreateFieldEvent extends AbstractSchemaEvent {

  private Entity entity;
  private TypedField<?, ?> field;

  public PreCreateFieldEvent() {
  }

  public PreCreateFieldEvent(String schemaName, Entity entity, TypedField<?, ?> field) {
    this.schemaName = schemaName;
    this.entity = entity;
    this.field = field;
  }

  public Entity getEntity() {
    return entity;
  }

  public void setEntity(Entity entity) {
    this.entity = entity;
  }

  public TypedField<?, ?> getField() {
    return field;
  }

  public void setField(TypedField<?, ?> field) {
    this.field = field;
  }
}
