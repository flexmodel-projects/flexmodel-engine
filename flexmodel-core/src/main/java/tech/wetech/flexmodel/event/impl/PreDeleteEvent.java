package tech.wetech.flexmodel.event.impl;

import tech.wetech.flexmodel.event.EventType;
import tech.wetech.flexmodel.event.PreChangeEvent;

/**
 * 前置删除事件
 *
 * @author cjbi
 */
public class PreDeleteEvent extends PreChangeEvent {
    
    public PreDeleteEvent(String modelName, String schemaName, Object oldData, Object id, String sessionId, Object source) {
        super(EventType.PRE_DELETE, modelName, schemaName, oldData, null, id, sessionId, source);
    }
}
