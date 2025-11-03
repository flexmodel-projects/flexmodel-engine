package tech.wetech.flexmodel.quarkus.session;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.session.Session;

/**
 * 事务拦截器
 * 用于自动管理事务的生命周期
 *
 * 该拦截器会在方法执行前开启事务，在方法正常返回时提交事务，
 * 在方法抛出异常时回滚事务
 *
 * 注意：
 * 1. 使用此注解的方法必须同时使用@SessionManaged注解
 * 2. TransactionalInterceptor的优先级应该低于SessionInterceptor，
 *    确保Session已经创建
 *
 * @author cjbi
 */
@Interceptor
@Transactional
@Priority(Interceptor.Priority.APPLICATION + 50)
public class TransactionalInterceptor {

  private static final Logger log = LoggerFactory.getLogger(TransactionalInterceptor.class);

  @Inject
  QuarkusSessionManager sessionManager;

  @AroundInvoke
  public Object aroundInvoke(InvocationContext context) throws Exception {
    log.debug("Transactional interceptor starting for method: {}", context.getMethod().getName());

    // 获取Transactional注解
    Transactional transactional = context.getMethod().getAnnotation(Transactional.class);
    if (transactional == null) {
      transactional = context.getTarget().getClass().getAnnotation(Transactional.class);
    }

    // 获取当前Session（应该已经由SessionInterceptor创建）
    Session session = sessionManager.getCurrentSessionOrNull();
    if (session == null) {
      throw new IllegalStateException(
          "No active Session found. Please ensure @SessionManaged annotation is present on the method or class.");
    }

    // 检查是否已有活跃的事务（嵌套调用场景）
    // 注意：这里简化处理，假设Session的状态可以告诉我们是否有活跃事务
    // 更精确的方法需要跟踪事务状态
    boolean transactionStarted = false;

    try {
      // 开启事务
      log.debug("Starting transaction for method: {}", context.getMethod().getName());
      session.startTransaction();
      transactionStarted = true;

      // 执行被拦截的方法
      Object result = context.proceed();

      // 提交事务
      log.debug("Committing transaction for method: {}", context.getMethod().getName());
      session.commit();

      log.debug("Transactional interceptor completed successfully for method: {}", context.getMethod().getName());
      return result;

    } catch (Exception e) {
      // 回滚事务
      if (transactionStarted) {
        log.error("Rolling back transaction due to exception in method: {}", context.getMethod().getName(), e);
        try {
          session.rollback();
        } catch (Exception rollbackException) {
          log.error("Failed to rollback transaction", rollbackException);
          // 不掩盖原始异常
        }
      }
      throw e;
    }
  }
}

