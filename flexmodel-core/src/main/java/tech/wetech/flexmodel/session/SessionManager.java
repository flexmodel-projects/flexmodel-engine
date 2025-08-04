package tech.wetech.flexmodel.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通用Session管理器
 * 负责在请求范围内管理多个Session的生命周期
 *
 * @author cjbi
 */
public class SessionManager {

  private static final Logger log = LoggerFactory.getLogger(SessionManager.class);

  protected SessionFactory sessionFactory;
  protected final ThreadLocal<Map<String, Session>> sessionMapHolder = new ThreadLocal<>();

  public SessionManager() {
    // 默认构造函数，用于继承
  }

  public SessionManager(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  /**
   * 获取指定模式的Session
   * 如果不存在则创建一个新的Session
   *
   * @param schemaName 模式名称
   * @return Session实例
   */
  public Session getSession(String schemaName) {
    Map<String, Session> sessionMap = getSessionMap();
    Session session = sessionMap.get(schemaName);
    if (session == null) {
      log.debug("Creating new session for schema: {}", schemaName);
      session = sessionFactory.createSession(schemaName);
      sessionMap.put(schemaName, session);
    }
    return session;
  }

  /**
   * 获取默认模式的Session
   *
   * @return Session实例
   */
  public Session getSession() {
    String defaultSchema = sessionFactory.getDefaultSchema();
    return getSession(defaultSchema);
  }

  /**
   * 关闭指定模式的Session
   *
   * @param schemaName 模式名称
   */
  public void closeSession(String schemaName) {
    Map<String, Session> sessionMap = getSessionMap();
    Session session = sessionMap.remove(schemaName);
    if (session != null) {
      try {
        log.debug("Closing session for schema: {}", schemaName);
        session.close();
      } catch (Exception e) {
        log.error("Error closing session for schema {}: {}", schemaName, e.getMessage(), e);
      }
    }
  }

  /**
   * 关闭所有Session
   */
  public void closeAllSessions() {
    Map<String, Session> sessionMap = getSessionMap();
    if (!sessionMap.isEmpty()) {
      log.debug("Closing all sessions, count: {}", sessionMap.size());
      sessionMap.values().forEach(session -> {
        try {
          session.close();
        } catch (Exception e) {
          log.error("Error closing session: {}", e.getMessage(), e);
        }
      });
      sessionMap.clear();
    }
    sessionMapHolder.remove();
  }

  /**
   * 检查指定模式是否有活跃的Session
   *
   * @param schemaName 模式名称
   * @return 如果有活跃Session返回true，否则返回false
   */
  public boolean hasActiveSession(String schemaName) {
    Map<String, Session> sessionMap = getSessionMap();
    return sessionMap.containsKey(schemaName);
  }

  /**
   * 检查是否有任何活跃的Session
   *
   * @return 如果有活跃Session返回true，否则返回false
   */
  public boolean hasActiveSession() {
    Map<String, Session> sessionMap = getSessionMap();
    return !sessionMap.isEmpty();
  }

  /**
   * 获取指定模式的当前Session（不创建新的）
   *
   * @param schemaName 模式名称
   * @return 当前Session，如果不存在则返回null
   */
  public Session getCurrentSession(String schemaName) {
    Map<String, Session> sessionMap = getSessionMap();
    return sessionMap.get(schemaName);
  }

  /**
   * 获取所有活跃的Session数量
   *
   * @return Session数量
   */
  public int getActiveSessionCount() {
    Map<String, Session> sessionMap = getSessionMap();
    return sessionMap.size();
  }

  /**
   * 获取SessionFactory
   *
   * @return SessionFactory实例
   */
  public SessionFactory getSessionFactory() {
    return sessionFactory;
  }

  /**
   * 获取或创建Session映射表
   *
   * @return Session映射表
   */
  private Map<String, Session> getSessionMap() {
    Map<String, Session> sessionMap = sessionMapHolder.get();
    if (sessionMap == null) {
      sessionMap = new ConcurrentHashMap<>();
      sessionMapHolder.set(sessionMap);
    }
    return sessionMap;
  }
}
