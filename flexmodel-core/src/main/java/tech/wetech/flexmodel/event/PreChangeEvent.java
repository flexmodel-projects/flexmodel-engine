package tech.wetech.flexmodel.event;

import tech.wetech.flexmodel.query.Query;

import java.util.Map;

/**
 * 前置事件基类
 *
 * @author cjbi
 */
public abstract class PreChangeEvent extends BaseEvent {

    private Map<String, Object> newData;
    private final Object id;
    private final Map<String, Object> oldData;
    private Query query;

    protected PreChangeEvent(EventType eventType, String modelName, String schemaName,
                             Map<String, Object> newData, Object id, String sessionId, Object source) {
        super(eventType, modelName, schemaName, sessionId, source);
        this.newData = newData;
        this.id = id;
        this.oldData = null;
    }

    protected PreChangeEvent(EventType eventType, String modelName, String schemaName,
                             Map<String, Object> oldData, Map<String, Object> newData, Object id, Query query, String sessionId, Object source) {
        super(eventType, modelName, schemaName, sessionId, source);
        this.oldData = oldData;
        this.newData = newData;
        this.id = id;
        this.query = query;
    }

    /**
     * 获取新数据
     *
     * @return 新数据
     */
    public Map<String, Object> getNewData() {
        return newData;
    }

    /**
     * 设置新数据（允许事件处理器修改）
     *
     * @param newData 新数据
     */
    public void setNewData(Map<String, Object> newData) {
        this.newData = newData;
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

    /**
     * 获取查询对象
     *
     * @return 查询对象
     */
    public Query getQuery() {
        return query;
    }

    /**
     * 设置查询对象（允许事件处理器修改）
     *
     * @param query 查询对象
     */
    public void setQuery(Query query) {
        this.query = query;
    }
}
