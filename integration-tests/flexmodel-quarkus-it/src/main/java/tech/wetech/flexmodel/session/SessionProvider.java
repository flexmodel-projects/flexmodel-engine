package tech.wetech.flexmodel.session;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Session提供者
 * 用于在CDI容器中提供Session实例
 *
 * @author cjbi
 */
@RequestScoped
public class SessionProvider {

  private static final Logger log = LoggerFactory.getLogger(SessionProvider.class);

  @Inject
  private QuarkusSessionManager sessionManager;

  /**
   * 提供默认Session
   *
   * @return Session实例
   */
  @Produces
  @RequestScoped
  public Session provideSession() {
    log.debug("Providing default session");
    return sessionManager.getSession();
  }

  /**
   * 提供指定模式的Session
   *
   * @param schemaName 模式名称
   * @return Session实例
   */
  public Session provideSession(String schemaName) {
    log.debug("Providing session for schema: {}", schemaName);
    return sessionManager.getSession(schemaName);
  }

  /**
   * 获取当前活跃的Session（不创建新的）
   *
   * @return 当前Session，如果不存在则返回null
   */
  public Session getCurrentSession() {
    return sessionManager.getCurrentSession();
  }

  /**
   * 检查当前是否有活跃的Session
   *
   * @return 如果有活跃Session返回true，否则返回false
   */
  public boolean hasActiveSession() {
    return sessionManager.hasActiveSession();
  }
}
