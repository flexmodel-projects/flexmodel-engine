package tech.wetech.flexmodel.event.record;

import tech.wetech.flexmodel.event.DomainEvent;

import java.time.LocalDateTime;

/**
 * @author cjbi
 */
public abstract class AbstractRecordEvent implements DomainEvent {

  protected String schemaName;

  private final LocalDateTime occurredOn;

  public AbstractRecordEvent() {
    this.occurredOn = LocalDateTime.now();
  }

  @Override
  public LocalDateTime occurredOn() {
    return occurredOn;
  }

  public String getSchemaName() {
    return schemaName;
  }

  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }
}
