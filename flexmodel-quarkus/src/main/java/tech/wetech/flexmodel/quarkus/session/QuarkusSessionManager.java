package tech.wetech.flexmodel.quarkus.session;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.wetech.flexmodel.session.Session;
import tech.wetech.flexmodel.session.SessionFactory;
import tech.wetech.flexmodel.session.SessionManager;

/**
 * Quarkus Session管理器
 * 继承通用SessionManager，集成CDI容器
 *
 * 该管理器与上下文传播机制集成，支持在异步操作中自动传递Session状态
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

  /**
   * 获取当前Session（不创建新的）
   * 如果当前没有活跃的Session，返回null
   *
   * @return 当前Session，如果不存在则返回null
   */
  public Session getCurrentSessionOrNull() {
    String currentSchema = getLastUsedSchema();
    if (currentSchema == null) {
      currentSchema = getSessionFactory().getDefaultSchema();
    }
    return getSession(currentSchema);
//    if (hasActiveSession(currentSchema)) {
//      return getSession(currentSchema);
//    }
//    return null;
  }

  /**
   * 获取最后使用的schema名称
   * 使用反射访问protected字段
   */
  @SuppressWarnings("unchecked")
  private String getLastUsedSchema() {
    try {
      java.lang.reflect.Field field = SessionManager.class.getDeclaredField("lastUsedSchemaHolder");
      field.setAccessible(true);
      ThreadLocal<String> holder = (ThreadLocal<String>) field.get(this);
      if (holder != null) {
        return holder.get();
      }
    } catch (Exception e) {
      // 忽略
    }
    return null;
  }
}

