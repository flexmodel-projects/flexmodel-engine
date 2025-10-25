package tech.wetech.flexmodel.event;

import tech.wetech.flexmodel.session.SessionFactory;

/**
 * 简化的事件基类
 *
 * @author cjbi
 */
public abstract class BaseEvent {

  private final EventType eventType;
  private final String modelName;
  private final String schemaName;
  private final long timestamp;
  private final String sessionId;
  private final SessionFactory source;

  protected BaseEvent(EventType eventType, String modelName, String schemaName,
                      String sessionId, SessionFactory source) {
    this.eventType = eventType;
    this.modelName = modelName;
    this.schemaName = schemaName;
    this.sessionId = sessionId;
    this.source = source;
    this.timestamp = System.currentTimeMillis();
  }

  public String getEventType() {
    return eventType.getValue();
  }

  public String getModelName() {
    return modelName;
  }

  public String getSchemaName() {
    return schemaName;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public String getSessionId() {
    return sessionId;
  }

  public SessionFactory getSource() {
    return source;
  }

  /**
   * 获取事件类型枚举
   *
   * @return 事件类型枚举
   */
  public EventType getEventTypeEnum() {
    return eventType;
  }
}
