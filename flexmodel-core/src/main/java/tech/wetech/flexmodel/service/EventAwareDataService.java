package tech.wetech.flexmodel.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.event.EventPublisher;
import tech.wetech.flexmodel.event.impl.*;
import tech.wetech.flexmodel.model.EntityDefinition;
import tech.wetech.flexmodel.model.SchemaObject;
import tech.wetech.flexmodel.model.field.TypedField;
import tech.wetech.flexmodel.query.Query;
import tech.wetech.flexmodel.reflect.ReflectionUtils;
import tech.wetech.flexmodel.session.SessionFactory;
import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter;

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
  private final Object source;

  public EventAwareDataService(DataService delegate, EventPublisher eventPublisher, String schemaName, String sessionId, Object source) {
    this.delegate = delegate;
    this.eventPublisher = eventPublisher;
    this.schemaName = schemaName;
    this.sessionId = sessionId;
    this.source = source;
  }

  @Override
  public int insert(String modelName, Object record) {
    log.debug("Starting insert operation for model: {}", modelName);

    // 发布前置事件
    PreInsertEvent preEvent = new PreInsertEvent(modelName, schemaName, record, extractId(modelName, record), sessionId, source);
    eventPublisher.publishPreChangeEvent(preEvent);

    Object oldData = null; // 插入前没有数据
    int affectedRows = 0;
    Throwable exception = null;
    boolean success = false;

    try {
      // 执行插入操作
      affectedRows = delegate.insert(modelName, record);
      success = affectedRows > 0;
      log.debug("Insert operation completed for model: {}, affected rows: {}", modelName, affectedRows);
    } catch (Throwable e) {
      exception = e;
      log.error("Insert operation failed for model: {}", modelName, e);
      throw e;
    } finally {
      // 发布后置事件
      InsertedEvent changedEvent = new InsertedEvent(
        modelName, schemaName, oldData, record, extractId(modelName, record),
        affectedRows, success, exception, sessionId, source
      );
      eventPublisher.publishChangedEvent(changedEvent);
    }

    return affectedRows;
  }

  @Override
  public int updateById(String modelName, Object record, Object id) {
    log.debug("Starting update operation for model: {}, id: {}", modelName, id);

    // 获取更新前的数据
    Object oldData = delegate.findById(modelName, id);

    // 发布前置事件
    PreUpdateEvent preEvent = new PreUpdateEvent(modelName, schemaName, oldData, record, id, sessionId, source);
    eventPublisher.publishPreChangeEvent(preEvent);

    int affectedRows = 0;
    Throwable exception = null;
    boolean success = false;

    try {
      // 执行更新操作
      affectedRows = delegate.updateById(modelName, record, id);
      success = affectedRows > 0;
      log.debug("Update operation completed for model: {}, affected rows: {}", modelName, affectedRows);
    } catch (Throwable e) {
      exception = e;
      log.error("Update operation failed for model: {}", modelName, e);
      throw e;
    } finally {
      // 发布后置事件
      UpdatedEvent changedEvent = new UpdatedEvent(
        modelName, schemaName, oldData, record, id,
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
    Object oldData = delegate.findById(modelName, id);

    // 发布前置事件
    PreDeleteEvent preEvent = new PreDeleteEvent(modelName, schemaName, oldData, id, sessionId, source);
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
  public int update(String modelName, Object record, String filter) {
    log.debug("Starting update operation for model: {} with filter: {}", modelName, filter);

    // 对于批量更新，我们无法获取所有旧数据，所以只发布后置事件
    int affectedRows = 0;
    Throwable exception = null;
    boolean success = false;

    try {
      // 执行更新操作
      affectedRows = delegate.update(modelName, record, filter);
      success = affectedRows > 0;
      log.debug("Update operation completed for model: {}, affected rows: {}", modelName, affectedRows);
    } catch (Throwable e) {
      exception = e;
      log.error("Update operation failed for model: {}", modelName, e);
      throw e;
    } finally {
      // 发布后置事件（批量更新没有前置事件）
      UpdatedEvent changedEvent = new UpdatedEvent(
        modelName, schemaName, null, record, null,
        affectedRows, success, exception, sessionId, source
      );
      eventPublisher.publishChangedEvent(changedEvent);
    }

    return affectedRows;
  }

  @Override
  public int delete(String modelName, String filter) {
    log.debug("Starting delete operation for model: {} with filter: {}", modelName, filter);

    // 对于批量删除，我们无法获取所有旧数据，所以只发布后置事件
    int affectedRows = 0;
    Throwable exception = null;
    boolean success = false;

    try {
      // 执行删除操作
      affectedRows = delegate.delete(modelName, filter);
      success = affectedRows > 0;
      log.debug("Delete operation completed for model: {}, affected rows: {}", modelName, affectedRows);
    } catch (Throwable e) {
      exception = e;
      log.error("Delete operation failed for model: {}", modelName, e);
      throw e;
    } finally {
      // 发布后置事件（批量删除没有前置事件）
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
  private Object extractId(String modelName, Object record) {
    try {
      // 这里需要根据实际的模型定义来提取ID
      Map<String, Object> data;
      if (record instanceof Map) {
        data = (Map<String, Object>) record;
      } else {
        data = ReflectionUtils.toClassBean(new JacksonObjectConverter(), record, Map.class);
      }
      if (source instanceof SessionFactory sf) {
        SchemaObject schemaObject = sf.getModelRegistry().getRegistered(schemaName, modelName);
        EntityDefinition entity = (EntityDefinition) schemaObject;
        TypedField<?, ?> field = entity.findIdField().orElseThrow();
        return data.get(field.getName());
      }
      return null;
    } catch (Exception e) {
      log.debug("Failed to extract ID from record: {}", e.getMessage());
      return null;
    }
  }

  // 委托其他方法给原始DataService
  @Override
  public <T> T findById(String modelName, Object id, Class<T> resultType, boolean nestedQuery) {
    return delegate.findById(modelName, id, resultType, nestedQuery);
  }

  @Override
  public <T> List<T> find(String modelName, Query query, Class<T> resultType) {
    return delegate.find(modelName, query, resultType);
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
    return delegate.count(modelName, query);
  }
}
