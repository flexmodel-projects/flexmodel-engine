package tech.wetech.flexmodel.event.impl;

import tech.wetech.flexmodel.event.ChangedEvent;
import tech.wetech.flexmodel.event.EventType;
import tech.wetech.flexmodel.session.SessionFactory;

import java.util.Map;

/**
 * 删除完成事件
 *
 * @author cjbi
 */
public class DeletedEvent extends ChangedEvent {

    public DeletedEvent(String modelName, String schemaName, Map<String, Object> oldData, Map<String, Object> newData, Object id,
                        int affectedRows, boolean success, Throwable exception, String sessionId, SessionFactory source) {
        super(EventType.DELETED, modelName, schemaName, oldData, newData, id, affectedRows, success, exception, sessionId, source);
    }
}
