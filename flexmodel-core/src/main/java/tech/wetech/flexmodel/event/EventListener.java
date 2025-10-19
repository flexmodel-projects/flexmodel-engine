package tech.wetech.flexmodel.event;

/**
 * 统一的事件监听器接口
 * 支持前置事件和后置事件的处理
 *
 * @author cjbi
 */
public interface EventListener {

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
     * 是否支持指定的事件类型
     * 对于前置事件，返回是否支持该前置事件类型
     * 对于后置事件，返回是否支持该后置事件类型
     *
     * @param eventType 事件类型
     * @return 是否支持
     */
    boolean supports(String eventType);

    /**
     * 获取监听器优先级，数字越小优先级越高
     *
     * @return 优先级
     */
    int getOrder();
}
