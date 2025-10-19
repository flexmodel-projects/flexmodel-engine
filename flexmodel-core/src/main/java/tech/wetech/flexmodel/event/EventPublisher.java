package tech.wetech.flexmodel.event;

/**
 * 事件发布器接口
 *
 * @author cjbi
 */
public interface EventPublisher {

    /**
     * 发布前置事件
     *
     * @param event 前置事件
     */
    void publishPreChangeEvent(PreChangeEvent event);

    /**
     * 发布后置事件
     *
     * @param event 后置事件
     */
    void publishChangedEvent(ChangedEvent event);

    /**
     * 添加前置事件监听器
     *
     * @param listener 监听器
     */
    void addPreChangeListener(PreChangeEventListener listener);

    /**
     * 添加后置事件监听器
     *
     * @param listener 监听器
     */
    void addChangedListener(ChangedEventListener listener);

    /**
     * 添加通用事件监听器
     *
     * @param listener 监听器
     */
    void addListener(FlexmodelEventListener listener);

    /**
     * 移除监听器
     *
     * @param listener 监听器
     */
    void removeListener(Object listener);
}
