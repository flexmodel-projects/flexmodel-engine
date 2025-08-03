package tech.wetech.flexmodel.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 通用Session管理器
 * 负责在请求范围内管理Session的生命周期
 *
 * @author cjbi
 */
public class SessionManager {

  private static final Logger log = LoggerFactory.getLogger(SessionManager.class);

  protected SessionFactory sessionFactory;
  protected final ThreadLocal<Session> sessionHolder = new ThreadLocal<>();

  public SessionManager() {
    // 默认构造函数，用于继承
  }

  public SessionManager(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  /**
   * 获取当前请求的Session
   * 如果不存在则创建一个新的Session
   *
   * @param schemaName 模式名称
   * @return Session实例
   */
  public Session getSession(String schemaName) {
    Session session = sessionHolder.get();
    if (session == null) {
      log.debug("Creating new session for schema: {}", schemaName);
      session = sessionFactory.createSession(schemaName);
      sessionHolder.set(session);
    }
    return session;
  }

  /**
   * 获取当前请求的Session（使用默认模式）
   *
   * @return Session实例
   */
  public Session getSession() {
    // 使用第一个可用的模式名称作为默认值
    String defaultSchema = sessionFactory.getSchemaNames().iterator().next();
    return getSession(defaultSchema);
  }

  /**
   * 关闭当前请求的Session
   */
  public void closeSession() {
    Session session = sessionHolder.get();
    if (session != null) {
      try {
        log.debug("Closing session: {}", session.getName());
        session.close();
      } catch (Exception e) {
        log.error("Error closing session: {}", e.getMessage(), e);
      } finally {
        sessionHolder.remove();
      }
    }
  }

  /**
   * 检查当前是否有活跃的Session
   *
   * @return 如果有活跃Session返回true，否则返回false
   */
  public boolean hasActiveSession() {
    return sessionHolder.get() != null;
  }

  /**
   * 获取当前活跃的Session（不创建新的）
   *
   * @return 当前Session，如果不存在则返回null
   */
  public Session getCurrentSession() {
    return sessionHolder.get();
  }

  /**
   * 获取SessionFactory
   *
   * @return SessionFactory实例
   */
  public SessionFactory getSessionFactory() {
    return sessionFactory;
  }
}
