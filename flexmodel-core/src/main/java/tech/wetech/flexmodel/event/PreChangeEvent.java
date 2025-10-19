package tech.wetech.flexmodel.event;

/**
 * 前置事件基类
 *
 * @author cjbi
 */
public abstract class PreChangeEvent extends BaseEvent {

    private final Object newData;
    private final Object id;
    private final Object oldData;

    protected PreChangeEvent(EventType eventType, String modelName, String schemaName, 
                             Object newData, Object id, String sessionId, Object source) {
        super(eventType, modelName, schemaName, sessionId, source);
        this.newData = newData;
        this.id = id;
        this.oldData = null;
    }

    protected PreChangeEvent(EventType eventType, String modelName, String schemaName, 
                             Object oldData, Object newData, Object id, String sessionId, Object source) {
        super(eventType, modelName, schemaName, sessionId, source);
        this.oldData = oldData;
        this.newData = newData;
        this.id = id;
    }

    /**
     * 获取新数据
     *
     * @return 新数据
     */
    public Object getNewData() {
        return newData;
    }

    /**
     * 获取记录ID
     *
     * @return 记录ID
     */
    public Object getId() {
        return id;
    }

    /**
     * 获取旧数据
     *
     * @return 旧数据
     */
    public Object getOldData() {
        return oldData;
    }
}
