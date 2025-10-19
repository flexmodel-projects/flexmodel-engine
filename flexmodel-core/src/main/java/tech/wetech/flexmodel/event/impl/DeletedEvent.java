package tech.wetech.flexmodel.event.impl;

import tech.wetech.flexmodel.event.ChangedEvent;
import tech.wetech.flexmodel.event.EventType;

/**
 * 删除完成事件
 *
 * @author cjbi
 */
public class DeletedEvent extends ChangedEvent {
    
    public DeletedEvent(String modelName, String schemaName, Object oldData, Object newData, Object id, 
                       int affectedRows, boolean success, Throwable exception, String sessionId, Object source) {
        super(EventType.DELETED, modelName, schemaName, oldData, newData, id, affectedRows, success, exception, sessionId, source);
    }
}
