package tech.wetech.flexmodel.quarkus.session;

import io.quarkus.arc.Arc;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Session拦截器
 * 用于在方法执行前创建Session，在方法执行后销毁Session
 *
 * 该拦截器自动激活RequestContext，确保在异步操作中可以访问CDI Bean
 * 支持通过上下文传播机制在异步操作中传递Session状态
 *
 * @author cjbi
 */
@Interceptor
@SessionManaged
@Priority(Interceptor.Priority.APPLICATION + 100)
public class SessionInterceptor {

  private static final Logger log = LoggerFactory.getLogger(SessionInterceptor.class);

  @Inject
  QuarkusSessionManager sessionManager;

  @AroundInvoke
  public Object aroundInvoke(InvocationContext context) throws Exception {
    log.debug("Session interceptor starting for method: {}", context.getMethod().getName());

    // 检查是否已有活跃的Session（嵌套调用场景）
    boolean hasActiveSession = sessionManager.hasActiveSession();

    // 激活RequestContext，确保CDI上下文在异步操作中可用
    var requestContext = Arc.container().requestContext();
    boolean contextWasActive = requestContext.isActive();
    if (!contextWasActive) {
      log.debug("Activating RequestContext");
      requestContext.activate();
    }

    try {
      // 获取SessionManaged注解
      SessionManaged sessionManaged = context.getMethod().getAnnotation(SessionManaged.class);
      if (sessionManaged == null) {
        sessionManaged = context.getTarget().getClass().getAnnotation(SessionManaged.class);
      }

      String schemaName = null;
      if (sessionManaged != null && !sessionManaged.schema().isEmpty()) {
        schemaName = sessionManaged.schema();
      }

      // 只有在没有活跃Session时才创建新的Session（避免嵌套调用时重复创建）
      if (!hasActiveSession) {
        if (schemaName != null) {
          log.debug("Creating session for schema: {}", schemaName);
          sessionManager.getSession(schemaName);
        } else {
          log.debug("Creating default session");
          sessionManager.getCurrentSession();
        }
      } else {
        log.debug("Session already active, reusing existing session");
        // 如果指定了schema但已有Session，确保使用指定的schema
        if (schemaName != null) {
          sessionManager.getSession(schemaName);
        }
      }

      // 执行被拦截的方法
      Object result = context.proceed();

      log.debug("Session interceptor completed successfully for method: {}", context.getMethod().getName());
      return result;

    } catch (Exception e) {
      log.error("Session interceptor caught exception for method: {}", context.getMethod().getName(), e);
      throw e;
    } finally {
      // 只有在没有活跃Session的情况下才清理（避免嵌套调用时过早清理）
      // 这里我们需要检查当前线程中是否还有其他活跃的Session
      // 简化处理：如果这是最外层的调用，清理所有Session
      // 更精确的方法需要跟踪嵌套层级，这里先简化
      if (!hasActiveSession) {
        log.debug("Closing all sessions after method: {}", context.getMethod().getName());
        sessionManager.closeAllSessions();
      }

      // 如果RequestContext是我们激活的，则取消激活
      if (!contextWasActive) {
        var rc = Arc.container().requestContext();
        if (rc.isActive()) {
          log.debug("Deactivating RequestContext");
          try {
            rc.deactivate();
          } catch (Exception e) {
            log.warn("Failed to deactivate RequestContext: {}", e.getMessage());
          }
        }
      }
    }
  }
}

