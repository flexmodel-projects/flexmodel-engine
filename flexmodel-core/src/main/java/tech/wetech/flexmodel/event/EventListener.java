package tech.wetech.flexmodel.event;

/**
 * 简化的事件监听器接口
 * 
 * @param <T> 事件类型
 * @author cjbi
 */
public interface EventListener<T extends FlexmodelEvent> {
    
    /**
     * 处理事件
     * 
     * @param event 事件对象
     */
    void handle(T event);
    
    /**
     * 获取优先级，数字越小优先级越高
     * 
     * @return 优先级
     */
    default int getPriority() {
        return 100;
    }
}
