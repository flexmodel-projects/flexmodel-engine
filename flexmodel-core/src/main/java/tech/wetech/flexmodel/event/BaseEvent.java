package tech.wetech.flexmodel.event;

/**
 * 简化的事件基类
 * 
 * @author cjbi
 */
public abstract class BaseEvent implements FlexmodelEvent {
    
    private final EventType eventType;
    private final String modelName;
    private final String schemaName;
    private final long timestamp;
    private final String sessionId;
    private final Object source;
    
    protected BaseEvent(EventType eventType, String modelName, String schemaName, 
                       String sessionId, Object source) {
        this.eventType = eventType;
        this.modelName = modelName;
        this.schemaName = schemaName;
        this.sessionId = sessionId;
        this.source = source;
        this.timestamp = System.currentTimeMillis();
    }
    
    @Override
    public String getEventType() {
        return eventType.getValue();
    }
    
    @Override
    public String getModelName() {
        return modelName;
    }
    
    @Override
    public String getSchemaName() {
        return schemaName;
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String getSessionId() {
        return sessionId;
    }
    
    @Override
    public Object getSource() {
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
