package tech.wetech.flexmodel.event;

/**
 * 事件类型枚举
 * 
 * @author cjbi
 */
public enum EventType {
    
    /**
     * 前置事件
     */
    PRE_INSERT("PRE_INSERT"),
    PRE_UPDATE("PRE_UPDATE"),
    PRE_DELETE("PRE_DELETE"),
    PRE_QUERY("PRE_QUERY"),
    
    /**
     * 后置事件
     */
    INSERTED("INSERTED"),
    UPDATED("UPDATED"),
    DELETED("DELETED");
    
    private final String value;
    
    EventType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return value;
    }
}
