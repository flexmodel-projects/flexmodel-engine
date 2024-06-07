package tech.wetech.flexmodel.event.schema;

import tech.wetech.flexmodel.Entity;

/**
 * @author cjbi
 */
public class PostCreateEntityEvent extends AbstractSchemaEvent {

  private Entity entity;

  public PostCreateEntityEvent() {
  }

  public PostCreateEntityEvent(String schemaName, Entity entity) {
    this.schemaName = schemaName;
    this.entity = entity;
  }

  public Entity getEntity() {
    return entity;
  }

  public void setEntity(Entity entity) {
    this.entity = entity;
  }
}
