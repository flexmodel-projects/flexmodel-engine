package tech.wetech.flexmodel.event.schema;

import tech.wetech.flexmodel.event.DomainEvent;

import java.time.LocalDateTime;

/**
 * @author cjbi
 */
public abstract class AbstractSchemaEvent implements DomainEvent {

  private final LocalDateTime occurredOn;
  protected String schemaName;

  public AbstractSchemaEvent() {
    this.occurredOn = LocalDateTime.now();
  }

  @Override
  public LocalDateTime occurredOn() {
    return occurredOn;
  }

  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }

  public String getSchemaName() {
    return schemaName;
  }
}
