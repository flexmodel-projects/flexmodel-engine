package tech.wetech.flexmodel.mongodb;

import tech.wetech.flexmodel.reflect.LazyLoadInterceptor;
import tech.wetech.flexmodel.session.AbstractSession;

/**
 * MongoDB数据库的Session实现
 * 负责MongoDB数据库的事务管理和连接管理
 *
 * @author cjbi
 */
public class MongoSession extends AbstractSession {

  public MongoSession(MongoContext mongoContext) {
    super(mongoContext, new MongoDataService(mongoContext), new MongoSchemaService(mongoContext));
  }

  @Override
  public void startTransaction() {
    // MongoDB事务管理（如果需要的话）
    log.debug("Start Transaction Session {}", sessionId);
  }

  @Override
  public void commit() {
    // MongoDB事务提交（如果需要的话）
    log.debug("Commit Session {}", sessionId);
  }

  @Override
  public void rollback() {
    // MongoDB事务回滚（如果需要的话）
    log.debug("Rollback Session {}", sessionId);
  }

  @Override
  public void close() {
    // MongoDB连接关闭（如果需要的话）
    LazyLoadInterceptor.clear();
    log.debug("Close Session {}", sessionId);
  }

  @Override
  public boolean isClosed() {
    return false;
  }
}
