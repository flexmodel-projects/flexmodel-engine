package tech.wetech.flexmodel.event;

/**
 * 后置事件基类
 *
 * @author cjbi
 */
public abstract class ChangedEvent extends BaseEvent {

    private final Object oldData;
    private final Object newData;
    private final Object id;
    private final int affectedRows;
    private final boolean success;
    private final Throwable exception;

    protected ChangedEvent(EventType eventType, String modelName, String schemaName,
                           Object oldData, Object newData, Object id, int affectedRows,
                           boolean success, Throwable exception, String sessionId, Object source) {
        super(eventType, modelName, schemaName, sessionId, source);
        this.oldData = oldData;
        this.newData = newData;
        this.id = id;
        this.affectedRows = affectedRows;
        this.success = success;
        this.exception = exception;
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
     * 获取影响行数
     *
     * @return 影响行数
     */
    public int getAffectedRows() {
        return affectedRows;
    }

    /**
     * 操作是否成功
     *
     * @return 是否成功
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 获取异常（如果操作失败）
     *
     * @return 异常
     */
    public Throwable getException() {
        return exception;
    }
}
