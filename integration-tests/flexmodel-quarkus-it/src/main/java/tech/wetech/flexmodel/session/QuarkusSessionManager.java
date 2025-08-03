package tech.wetech.flexmodel.session;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Quarkus Session管理器
 * 继承通用SessionManager，集成CDI容器
 *
 * @author cjbi
 */
@ApplicationScoped
public class QuarkusSessionManager extends SessionManager {

  /**
   * 构造函数，通过CDI注入SessionFactory
   */
  @Inject
  public QuarkusSessionManager(SessionFactory sessionFactory) {
    super(sessionFactory);
  }
}
