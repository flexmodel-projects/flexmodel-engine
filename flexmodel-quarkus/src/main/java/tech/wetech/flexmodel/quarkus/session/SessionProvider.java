package tech.wetech.flexmodel.quarkus.session;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.session.Session;

/**
 * Session提供者
 * 用于在CDI容器中提供Session实例
 * 
 * 该提供者通过QuarkusSessionManager获取当前Session，支持在异步操作中访问
 * （通过上下文传播机制）
 * 
 * 注意：Session的生命周期由SessionInterceptor管理，此Provider只是提供访问入口
 *
 * @author cjbi
 */
@RequestScoped
public class SessionProvider {

  private static final Logger log = LoggerFactory.getLogger(SessionProvider.class);

  @Inject
  QuarkusSessionManager sessionManager;

  /**
   * 提供默认Session
   * 
   * @return Session实例
   */
  @Produces
  @RequestScoped
  public Session provideSession() {
    log.debug("Providing session via CDI");
    
    // 获取当前Session（如果不存在会创建，但这应该由SessionInterceptor负责）
    Session session = sessionManager.getCurrentSessionOrNull();
    
    if (session == null) {
      // 如果没有活跃的Session，尝试获取默认Session（这会创建新的Session）
      // 这种情况应该很少发生，因为SessionInterceptor应该已经创建了Session
      log.warn("No active session found, creating default session. " +
          "Consider using @SessionManaged annotation on your method or class.");
      session = sessionManager.getCurrentSession();
    }
    
    return session;
  }
}

