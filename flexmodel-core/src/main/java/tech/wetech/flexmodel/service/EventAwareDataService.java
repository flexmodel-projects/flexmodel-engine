package tech.wetech.flexmodel.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.event.EventPublisher;
import tech.wetech.flexmodel.event.impl.*;
import tech.wetech.flexmodel.model.EntityDefinition;
import tech.wetech.flexmodel.model.SchemaObject;
import tech.wetech.flexmodel.model.field.TypedField;
import tech.wetech.flexmodel.query.Query;
import tech.wetech.flexmodel.session.SessionFactory;

import java.util.List;
import java.util.Map;

/**
 * 事件感知的数据服务包装器
 *
 * @author cjbi
 */
public class EventAwareDataService implements DataService {

  private static final Logger log = LoggerFactory.getLogger(EventAwareDataService.class);

  private final DataService delegate;
  private final EventPublisher eventPublisher;
  private final String schemaName;
  private final String sessionId;
  private final SessionFactory source;

  public EventAwareDataService(DataService delegate, EventPublisher eventPublisher, String schemaName, String sessionId, SessionFactory source) {
    this.delegate = delegate;
    this.eventPublisher = eventPublisher;
    this.schemaName = schemaName;
    this.sessionId = sessionId;
    this.source = source;
  }

  @Override
  public int insert(String modelName, Map<String, Object> record) {
    log.debug("Starting insert operation for model: {}", modelName);

    // 发布前置事件
    PreInsertEvent preEvent = new PreInsertEvent(modelName, schemaName, record, extractId(modelName, record), sessionId, source);
    eventPublisher.publishPreChangeEvent(preEvent);

    // 使用事件中可能被修改的数据
    Map<String, Object> finalRecord = preEvent.getNewData() != null ? preEvent.getNewData() : record;

    int affectedRows = 0;
    Throwable exception = null;
    boolean success = false;

    try {
      // 执行插入操作
      affectedRows = delegate.insert(modelName, finalRecord);
      success = affectedRows > 0;
      log.debug("Insert operation completed for model: {}, affected rows: {}", modelName, affectedRows);
    } catch (Throwable e) {
      exception = e;
      log.error("Insert operation failed for model: {}", modelName, e);
      throw e;
    } finally {
      // 发布后置事件
      InsertedEvent changedEvent = new InsertedEvent(
        modelName, schemaName, finalRecord, record, extractId(modelName, finalRecord),
        affectedRows, success, exception, sessionId, source
      );
      eventPublisher.publishChangedEvent(changedEvent);
    }

    return affectedRows;
  }

  @Override
  public int updateById(String modelName, Map<String, Object> record, Object id) {
    log.debug("Starting update operation for model: {}, id: {}", modelName, id);

    // 获取更新前的数据
    Map<String, Object> oldData = delegate.findById(modelName, id);

    // 发布前置事件
    PreUpdateEvent preEvent = new PreUpdateEvent(modelName, schemaName, oldData, record, id, null, sessionId, source);
    eventPublisher.publishPreChangeEvent(preEvent);

    // 使用事件中可能被修改的数据
    Map<String, Object> finalRecord = preEvent.getNewData() != null ? preEvent.getNewData() : record;

    int affectedRows = 0;
    Throwable exception = null;
    boolean success = false;

    try {
      // 执行更新操作
      affectedRows = delegate.updateById(modelName, finalRecord, id);
      success = affectedRows > 0;
      log.debug("Update operation completed for model: {}, affected rows: {}", modelName, affectedRows);
    } catch (Throwable e) {
      exception = e;
      log.error("Update operation failed for model: {}", modelName, e);
      throw e;
    } finally {
      // 发布后置事件
      UpdatedEvent changedEvent = new UpdatedEvent(
        modelName, schemaName, oldData, finalRecord, id,
        affectedRows, success, exception, sessionId, source
      );
      eventPublisher.publishChangedEvent(changedEvent);
    }

    return affectedRows;
  }

  @Override
  public int deleteById(String modelName, Object id) {
    log.debug("Starting delete operation for model: {}, id: {}", modelName, id);

    // 获取删除前的数据
    Map<String, Object> oldData = delegate.findById(modelName, id);

    // 发布前置事件
    PreDeleteEvent preEvent = new PreDeleteEvent(modelName, schemaName, oldData, id, null, sessionId, source);
    eventPublisher.publishPreChangeEvent(preEvent);

    int affectedRows = 0;
    Throwable exception = null;
    boolean success = false;

    try {
      // 执行删除操作
      affectedRows = delegate.deleteById(modelName, id);
      success = affectedRows > 0;
      log.debug("Delete operation completed for model: {}, affected rows: {}", modelName, affectedRows);
    } catch (Throwable e) {
      exception = e;
      log.error("Delete operation failed for model: {}", modelName, e);
      throw e;
    } finally {
      // 发布后置事件
      DeletedEvent changedEvent = new DeletedEvent(
        modelName, schemaName, oldData, null, id,
        affectedRows, success, exception, sessionId, source
      );
      eventPublisher.publishChangedEvent(changedEvent);
    }

    return affectedRows;
  }

  @Override
  public int update(String modelName, Map<String, Object> record, String filter) {
    log.debug("Starting update operation for model: {} with filter: {}", modelName, filter);

    // 创建查询对象
    Query query = new Query();
    query.setFilter(filter);

    // 发布前置事件
    PreUpdateEvent preEvent = new PreUpdateEvent(modelName, schemaName, null, record, null, query, sessionId, source);
    eventPublisher.publishPreChangeEvent(preEvent);

    // 使用事件中可能被修改的数据和查询
    Map<String, Object> finalRecord = preEvent.getNewData() != null ? preEvent.getNewData() : record;
    Query finalQuery = preEvent.getQuery() != null ? preEvent.getQuery() : query;
    String finalFilter = finalQuery.getFilter();

    int affectedRows = 0;
    Throwable exception = null;
    boolean success = false;

    try {
      // 执行更新操作
      affectedRows = delegate.update(modelName, finalRecord, finalFilter);
      success = affectedRows > 0;
      log.debug("Update operation completed for model: {}, affected rows: {}", modelName, affectedRows);
    } catch (Throwable e) {
      exception = e;
      log.error("Update operation failed for model: {}", modelName, e);
      throw e;
    } finally {
      // 发布后置事件
      UpdatedEvent changedEvent = new UpdatedEvent(
        modelName, schemaName, null, finalRecord, null,
        affectedRows, success, exception, sessionId, source
      );
      eventPublisher.publishChangedEvent(changedEvent);
    }

    return affectedRows;
  }

  @Override
  public int delete(String modelName, String filter) {
    log.debug("Starting delete operation for model: {} with filter: {}", modelName, filter);

    // 创建查询对象
    Query query = new Query();
    query.setFilter(filter);

    // 发布前置事件
    PreDeleteEvent preEvent = new PreDeleteEvent(modelName, schemaName, null, null, query, sessionId, source);
    eventPublisher.publishPreChangeEvent(preEvent);

    // 使用事件中可能被修改的查询
    Query finalQuery = preEvent.getQuery() != null ? preEvent.getQuery() : query;
    String finalFilter = finalQuery.getFilter();

    int affectedRows = 0;
    Throwable exception = null;
    boolean success = false;

    try {
      // 执行删除操作
      affectedRows = delegate.delete(modelName, finalFilter);
      success = affectedRows > 0;
      log.debug("Delete operation completed for model: {}, affected rows: {}", modelName, affectedRows);
    } catch (Throwable e) {
      exception = e;
      log.error("Delete operation failed for model: {}", modelName, e);
      throw e;
    } finally {
      // 发布后置事件
      DeletedEvent changedEvent = new DeletedEvent(
        modelName, schemaName, null, null, null,
        affectedRows, success, exception, sessionId, source
      );
      eventPublisher.publishChangedEvent(changedEvent);
    }

    return affectedRows;
  }

  @Override
  public int deleteAll(String modelName) {
    log.debug("Starting delete all operation for model: {}", modelName);

    // 对于删除所有记录，我们无法获取所有旧数据，所以只发布后置事件
    int affectedRows = 0;
    Throwable exception = null;
    boolean success = false;

    try {
      // 执行删除操作
      affectedRows = delegate.deleteAll(modelName);
      success = affectedRows > 0;
      log.debug("Delete all operation completed for model: {}, affected rows: {}", modelName, affectedRows);
    } catch (Throwable e) {
      exception = e;
      log.error("Delete all operation failed for model: {}", modelName, e);
      throw e;
    } finally {
      // 发布后置事件（删除所有没有前置事件）
      DeletedEvent changedEvent = new DeletedEvent(
        modelName, schemaName, null, null, null,
        affectedRows, success, exception, sessionId, source
      );
      eventPublisher.publishChangedEvent(changedEvent);
    }

    return affectedRows;
  }

  /**
   * 从记录中提取ID
   */
  private Object extractId(String modelName, Map<String, Object> data) {
    try {
      // 这里需要根据实际的模型定义来提取ID
      SchemaObject schemaObject = source.getModelRegistry().getRegistered(schemaName, modelName);
      EntityDefinition entity = (EntityDefinition) schemaObject;
      TypedField<?, ?> field = entity.findIdField().orElseThrow();
      return data.get(field.getName());
    } catch (Exception e) {
      log.debug("Failed to extract ID from record: {}", e.getMessage());
      return null;
    }
  }

  // 委托其他方法给原始DataService
  @Override
  public <T> T findById(String modelName, Object id, Class<T> resultType, boolean nestedQuery) {
    log.debug("Starting findById operation for model: {}, id: {}", modelName, id);

    // 发布前置查询事件
    PreQueryEvent preEvent = new PreQueryEvent(modelName, schemaName, null, sessionId, source);
    eventPublisher.publishPreChangeEvent(preEvent);

    try {
      T result = delegate.findById(modelName, id, resultType, nestedQuery);
      log.debug("FindById operation completed for model: {}, id: {}", modelName, id);
      return result;
    } catch (Exception e) {
      log.error("FindById operation failed for model: {}, id: {}", modelName, id, e);
      throw e;
    }
  }

  @Override
  public <T> List<T> find(String modelName, Query query, Class<T> resultType) {
    log.debug("Starting find operation for model: {}", modelName);

    // 发布前置查询事件
    PreQueryEvent preEvent = new PreQueryEvent(modelName, schemaName, query, sessionId, source);
    eventPublisher.publishPreChangeEvent(preEvent);

    // 使用事件中可能被修改的查询
    Query finalQuery = preEvent.getQuery() != null ? preEvent.getQuery() : query;

    try {
      List<T> result = delegate.find(modelName, finalQuery, resultType);
      log.debug("Find operation completed for model: {}, result count: {}", modelName, result.size());
      return result;
    } catch (Exception e) {
      log.error("Find operation failed for model: {}", modelName, e);
      throw e;
    }
  }

  @Override
  public <T> List<T> findByNativeQuery(String modelName, Object params, Class<T> resultType) {
    return delegate.findByNativeQuery(modelName, params, resultType);
  }

  @Override
  public Object executeNativeStatement(String statement, Object params) {
    return delegate.executeNativeStatement(statement, params);
  }

  @Override
  public long count(String modelName, Query query) {
    log.debug("Starting count operation for model: {}", modelName);

    // 发布前置查询事件
    PreQueryEvent preEvent = new PreQueryEvent(modelName, schemaName, query, sessionId, source);
    eventPublisher.publishPreChangeEvent(preEvent);

    // 使用事件中可能被修改的查询
    Query finalQuery = preEvent.getQuery() != null ? preEvent.getQuery() : query;

    try {
      long result = delegate.count(modelName, finalQuery);
      log.debug("Count operation completed for model: {}, count: {}", modelName, result);
      return result;
    } catch (Exception e) {
      log.error("Count operation failed for model: {}", modelName, e);
      throw e;
    }
  }
}
