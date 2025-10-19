package tech.wetech.flexmodel.event;

/**
 * FlexModel事件基础接口
 *
 * @author cjbi
 */
public interface FlexmodelEvent {

    /**
     * 获取事件类型
     *
     * @return 事件类型
     */
    String getEventType();

    /**
     * 获取模型名称
     *
     * @return 模型名称
     */
    String getModelName();

    /**
     * 获取模式名称
     *
     * @return 模式名称
     */
    String getSchemaName();

    /**
     * 获取事件时间戳
     *
     * @return 时间戳
     */
    long getTimestamp();

    /**
     * 获取事件源
     *
     * @return 事件源
     */
    Object getSource();

    /**
     * 获取会话ID
     *
     * @return 会话ID
     */
    String getSessionId();
}
