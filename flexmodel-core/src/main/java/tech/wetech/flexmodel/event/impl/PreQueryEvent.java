package tech.wetech.flexmodel.event.impl;

import tech.wetech.flexmodel.event.EventType;
import tech.wetech.flexmodel.event.PreChangeEvent;
import tech.wetech.flexmodel.query.Query;

/**
 * 前置查询事件
 *
 * @author cjbi
 */
public class PreQueryEvent extends PreChangeEvent {
    
    public PreQueryEvent(String modelName, String schemaName, Query query, String sessionId, Object source) {
        super(EventType.PRE_QUERY, modelName, schemaName, null, null, null, query, sessionId, source);
    }
}
