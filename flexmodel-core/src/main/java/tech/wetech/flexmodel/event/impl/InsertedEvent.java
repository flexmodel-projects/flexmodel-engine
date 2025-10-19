package tech.wetech.flexmodel.event.impl;

import tech.wetech.flexmodel.event.ChangedEvent;
import tech.wetech.flexmodel.event.EventType;

/**
 * 插入完成事件
 *
 * @author cjbi
 */
public class InsertedEvent extends ChangedEvent {
    
    public InsertedEvent(String modelName, String schemaName, Object oldData, Object newData, Object id, 
                       int affectedRows, boolean success, Throwable exception, String sessionId, Object source) {
        super(EventType.INSERTED, modelName, schemaName, oldData, newData, id, affectedRows, success, exception, sessionId, source);
    }
}
