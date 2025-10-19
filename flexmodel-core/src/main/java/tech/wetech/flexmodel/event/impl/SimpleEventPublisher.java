package tech.wetech.flexmodel.event.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.event.ChangedEvent;
import tech.wetech.flexmodel.event.EventListener;
import tech.wetech.flexmodel.event.EventPublisher;
import tech.wetech.flexmodel.event.PreChangeEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 简单的事件发布器实现
 *
 * @author cjbi
 */
public class SimpleEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SimpleEventPublisher.class);

    private final List<EventListener> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void publishPreChangeEvent(PreChangeEvent event) {
        if (log.isTraceEnabled()) {
            log.trace("Publishing pre-change event: {} for model: {}", event.getEventType(), event.getModelName());
        }

        // 获取支持该事件类型的监听器并按优先级排序
        List<EventListener> supportedListeners = listeners.stream()
            .filter(listener -> listener.supports(event.getEventType()))
            .sorted(Comparator.comparingInt(EventListener::getOrder))
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        // 执行所有监听器
        for (EventListener listener : supportedListeners) {
            try {
                listener.onPreChange(event);
            } catch (Exception e) {
                log.error("Error in pre-change event listener for event: {}", event.getEventType(), e);
            }
        }
    }

    @Override
    public void publishChangedEvent(ChangedEvent event) {
        if (log.isTraceEnabled()) {
            log.trace("Publishing changed event: {} for model: {}", event.getEventType(), event.getModelName());
        }

        // 获取支持该事件类型的监听器并按优先级排序
        List<EventListener> supportedListeners = listeners.stream()
            .filter(listener -> listener.supports(event.getEventType()))
            .sorted(Comparator.comparingInt(EventListener::getOrder))
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        // 执行所有监听器
        for (EventListener listener : supportedListeners) {
            try {
                listener.onChanged(event);
            } catch (Exception e) {
                log.error("Error in changed event listener for event: {}", event.getEventType(), e);
            }
        }
    }

    @Override
    public void addListener(EventListener listener) {
        if (listener != null) {
            listeners.add(listener);
            log.debug("Added event listener: {}", listener.getClass().getSimpleName());
        }
    }

    @Override
    public void removeListener(EventListener listener) {
        if (listener != null && listeners.remove(listener)) {
            log.debug("Removed event listener: {}", listener.getClass().getSimpleName());
        }
    }
}
