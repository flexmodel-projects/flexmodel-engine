package tech.wetech.flexmodel.event.impl;

import tech.wetech.flexmodel.event.EventType;
import tech.wetech.flexmodel.event.PreChangeEvent;
import tech.wetech.flexmodel.query.Query;

/**
 * 前置更新事件
 *
 * @author cjbi
 */
public class PreUpdateEvent extends PreChangeEvent {

    public PreUpdateEvent(String modelName, String schemaName, Object oldData, Object newData, Object id, Query query, String sessionId, Object source) {
        super(EventType.PRE_UPDATE, modelName, schemaName, oldData, newData, id, query, sessionId, source);
    }
}
