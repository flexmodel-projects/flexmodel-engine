package tech.wetech.flexmodel.session;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Session拦截器
 * 用于在方法执行前创建Session，在方法执行后销毁Session
 *
 * @author cjbi
 */
@Interceptor
@SessionManaged
@Priority(Interceptor.Priority.APPLICATION)
public class SessionInterceptor {

  private static final Logger log = LoggerFactory.getLogger(SessionInterceptor.class);

  @jakarta.inject.Inject
  QuarkusSessionManager sessionManager;

  @AroundInvoke
  public Object aroundInvoke(InvocationContext context) throws Exception {
    log.debug("Session interceptor starting for method: {}", context.getMethod().getName());

    // 获取SessionManaged注解
    SessionManaged sessionManaged = context.getMethod().getAnnotation(SessionManaged.class);
    if (sessionManaged == null) {
      sessionManaged = context.getTarget().getClass().getAnnotation(SessionManaged.class);
    }

    Session session;
    // 根据注解参数创建Session
    if (sessionManaged != null && !sessionManaged.schema().isEmpty()) {
      session = sessionManager.getSession(sessionManaged.schema());
    } else {
      session = sessionManager.getCurrentSession(); // 使用默认Session
    }

    try {
      // 执行被拦截的方法
      Object result = context.proceed();
      log.debug("Session interceptor completed successfully for method: {}", context.getMethod().getName());
      return result;
    } catch (Exception e) {
      log.error("Session interceptor caught exception for method: {}", context.getMethod().getName(), e);
      throw e;
    } finally {
      // 确保在方法执行完成后关闭Session
      sessionManager.closeSession(session.getName());
      log.debug("Session closed for method: {}", context.getMethod().getName());
    }
  }
}
