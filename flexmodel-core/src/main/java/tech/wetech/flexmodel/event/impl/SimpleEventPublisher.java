package tech.wetech.flexmodel.event.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.event.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 简单的事件发布器实现
 *
 * @author cjbi
 */
public class SimpleEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SimpleEventPublisher.class);

    private final List<PreChangeEventListener> preChangeListeners = new CopyOnWriteArrayList<>();
    private final List<ChangedEventListener> changedListeners = new CopyOnWriteArrayList<>();
    private final List<FlexmodelEventListener> generalListeners = new CopyOnWriteArrayList<>();

    @Override
    public void publishPreChangeEvent(PreChangeEvent event) {
        if (log.isTraceEnabled()) {
            log.trace("Publishing pre-change event: {} for model: {}", event.getEventType(), event.getModelName());
        }

        // 收集所有前置事件监听器
        List<PreChangeEventListener> allListeners = new ArrayList<>(preChangeListeners);

        // 添加通用监听器中的前置事件监听器
        allListeners.addAll(generalListeners.stream()
            .filter(l -> l.supportsPreChange(event.getEventType()))
            .map(l -> new PreChangeEventListener() {
                @Override
                public void onPreChange(PreChangeEvent preEvent) {
                    l.onPreChange(preEvent);
                }

                @Override
                public boolean supports(String eventType) {
                    return l.supportsPreChange(eventType);
                }

                @Override
                public int getOrder() {
                    return l.getOrder();
                }
            })
            .collect(Collectors.toList()));

        // 按优先级排序
        allListeners.sort(Comparator.comparingInt(PreChangeEventListener::getOrder));

        // 执行所有监听器
        for (PreChangeEventListener listener : allListeners) {
            if (listener.supports(event.getEventType())) {
                try {
                    listener.onPreChange(event);
                } catch (Exception e) {
                    log.error("Error in pre-change event listener for event: {}", event.getEventType(), e);
                }
            }
        }
    }

    @Override
    public void publishChangedEvent(ChangedEvent event) {
        if (log.isTraceEnabled()) {
            log.trace("Publishing changed event: {} for model: {}", event.getEventType(), event.getModelName());
        }

        // 收集所有后置事件监听器
        List<ChangedEventListener> allListeners = new ArrayList<>(changedListeners);

        // 添加通用监听器中的后置事件监听器
        allListeners.addAll(generalListeners.stream()
            .filter(l -> l.supportsChanged(event.getEventType()))
            .map(l -> new ChangedEventListener() {
                @Override
                public void onChanged(ChangedEvent changedEvent) {
                    l.onChanged(changedEvent);
                }

                @Override
                public boolean supports(String eventType) {
                    return l.supportsChanged(eventType);
                }

                @Override
                public int getOrder() {
                    return l.getOrder();
                }
            })
            .collect(Collectors.toList()));

        // 按优先级排序
        allListeners.sort(Comparator.comparingInt(ChangedEventListener::getOrder));

        // 执行所有监听器
        for (ChangedEventListener listener : allListeners) {
            if (listener.supports(event.getEventType())) {
                try {
                    listener.onChanged(event);
                } catch (Exception e) {
                    log.error("Error in changed event listener for event: {}", event.getEventType(), e);
                }
            }
        }
    }

    @Override
    public void addPreChangeListener(PreChangeEventListener listener) {
        if (listener != null) {
            preChangeListeners.add(listener);
            log.debug("Added pre-change event listener: {}", listener.getClass().getSimpleName());
        }
    }

    @Override
    public void addChangedListener(ChangedEventListener listener) {
        if (listener != null) {
            changedListeners.add(listener);
            log.debug("Added changed event listener: {}", listener.getClass().getSimpleName());
        }
    }

    @Override
    public void addListener(FlexmodelEventListener listener) {
        if (listener != null) {
            generalListeners.add(listener);
            log.debug("Added general event listener: {}", listener.getClass().getSimpleName());
        }
    }

    @Override
    public void removeListener(Object listener) {
        if (listener != null) {
            boolean removed = false;

            if (listener instanceof PreChangeEventListener) {
                removed = preChangeListeners.remove(listener) || removed;
            }

            if (listener instanceof ChangedEventListener) {
                removed = changedListeners.remove(listener) || removed;
            }

            if (listener instanceof FlexmodelEventListener) {
                removed = generalListeners.remove(listener) || removed;
            }

            if (removed) {
                log.debug("Removed event listener: {}", listener.getClass().getSimpleName());
            }
        }
    }
}
