package tech.wetech.flexmodel.event.impl;

import tech.wetech.flexmodel.event.EventType;
import tech.wetech.flexmodel.event.PreChangeEvent;
import tech.wetech.flexmodel.query.Query;

/**
 * 前置删除事件
 *
 * @author cjbi
 */
public class PreDeleteEvent extends PreChangeEvent {

    public PreDeleteEvent(String modelName, String schemaName, Object oldData, Object id, Query query, String sessionId, Object source) {
        super(EventType.PRE_DELETE, modelName, schemaName, oldData, null, id, query, sessionId, source);
    }
}
