package tech.wetech.flexmodel.event.schema;

import tech.wetech.flexmodel.Entity;
import tech.wetech.flexmodel.Index;

/**
 * @author cjbi
 */
public class PreCreateIndexEvent extends AbstractSchemaEvent {

  private Entity entity;
  private Index index;

  public PreCreateIndexEvent() {
  }

  public PreCreateIndexEvent(String schemaName, Entity entity, Index index) {
    this.schemaName = schemaName;
    this.entity = entity;
    this.index = index;
  }

  public Entity getEntity() {
    return entity;
  }

  public void setEntity(Entity entity) {
    this.entity = entity;
  }

  public Index getIndex() {
    return index;
  }

  public void setIndex(Index index) {
    this.index = index;
  }

}
