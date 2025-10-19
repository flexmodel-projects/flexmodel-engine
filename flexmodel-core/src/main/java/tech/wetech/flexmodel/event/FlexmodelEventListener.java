package tech.wetech.flexmodel.event;

/**
 * 通用事件监听器接口
 *
 * @author cjbi
 */
public interface FlexmodelEventListener {

    /**
     * 处理前置事件
     *
     * @param event 前置事件
     */
    void onPreChange(PreChangeEvent event);

    /**
     * 处理后置事件
     *
     * @param event 后置事件
     */
    void onChanged(ChangedEvent event);

    /**
     * 是否支持指定的前置事件类型
     *
     * @param eventType 事件类型
     * @return 是否支持
     */
    boolean supportsPreChange(String eventType);

    /**
     * 是否支持指定的后置事件类型
     *
     * @param eventType 事件类型
     * @return 是否支持
     */
    boolean supportsChanged(String eventType);

    /**
     * 获取监听器优先级，数字越小优先级越高
     *
     * @return 优先级
     */
    int getOrder();
}
