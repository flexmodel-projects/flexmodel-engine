package tech.wetech.flexmodel.event;

/**
 * 前置事件监听器接口
 *
 * @author cjbi
 */
public interface PreChangeEventListener {
    
    /**
     * 处理前置事件
     *
     * @param event 前置事件
     */
    void onPreChange(PreChangeEvent event);
    
    /**
     * 是否支持指定的事件类型
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
