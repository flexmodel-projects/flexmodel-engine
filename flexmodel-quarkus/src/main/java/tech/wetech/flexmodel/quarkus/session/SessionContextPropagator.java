package tech.wetech.flexmodel.quarkus.session;

import io.quarkus.arc.Arc;
import org.eclipse.microprofile.context.spi.ThreadContextProvider;
import org.eclipse.microprofile.context.spi.ThreadContextSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.session.SessionManager;

import java.util.Map;

/**
 * Session上下文传播器
 * 实现ThreadContextProvider接口，用于在异步操作中自动传播Session状态
 *
 * 支持Mutiny、CompletableFuture等异步框架的上下文传播
 *
 * 注意：ThreadContextProvider通过SPI机制实例化，不在CDI容器中，
 * 因此通过Arc容器动态获取SessionManager实例
 *
 * @author cjbi
 */
public class SessionContextPropagator implements ThreadContextProvider {

  private static final Logger log = LoggerFactory.getLogger(SessionContextPropagator.class);

  /**
   * 上下文类型名称
   */
  @Override
  public String getThreadContextType() {
    return "FlexModel-Session";
  }

  /**
   * 获取SessionManager实例
   * 通过Arc容器动态获取QuarkusSessionManager实例
   */
  private SessionManager getSessionManager() {
    try {
      QuarkusSessionManager manager = Arc.container().instance(QuarkusSessionManager.class).get();
      if (manager != null) {
        return manager;
      }
    } catch (Exception e) {
      log.debug("Failed to get QuarkusSessionManager from Arc container: {}", e.getMessage());
    }

    // 如果获取失败，尝试获取通用的SessionManager
    try {
      SessionManager manager = Arc.container().instance(SessionManager.class).get();
      if (manager != null) {
        return manager;
      }
    } catch (Exception e) {
      log.debug("Failed to get SessionManager from Arc container: {}", e.getMessage());
    }

    log.warn("No SessionManager found in CDI container, context propagation may not work");
    return null;
  }

  /**
   * 获取当前线程的上下文快照
   * 在异步操作开始前调用，捕获当前Session状态
   */
  @Override
  public ThreadContextSnapshot currentContext(Map<String, String> props) {
    log.debug("Capturing Session context for propagation");

    SessionManager sessionManager = getSessionManager();
    if (sessionManager == null) {
      log.debug("No SessionManager available, returning empty context");
      return () -> {
        log.debug("Restoring empty Session context (no SessionManager)");
        // 返回一个空的控制器（无操作）
        return () -> {};
      };
    }

    SessionContext context = SessionContext.from(sessionManager);

    if (context == null || context.isEmpty()) {
      log.debug("No active Session context to propagate");
      return () -> {
        // 空上下文，不做任何操作
        log.debug("Restoring empty Session context");
        // 返回一个空的控制器（无操作）
        return () -> {};
      };
    }

    log.debug("Captured Session context with schemas: {}, lastUsed: {}",
        context.getActiveSchemas(), context.getLastUsedSchema());

    // 需要在restore时重新获取SessionManager，因为它可能在不同线程中
    final SessionContext finalContext = context;
    return () -> {
      log.debug("Restoring Session context with schemas: {}, lastUsed: {}",
          finalContext.getActiveSchemas(), finalContext.getLastUsedSchema());
      SessionManager manager = getSessionManager();
      if (manager != null) {
        SessionContext.restoreTo(manager, finalContext);
      } else {
        log.warn("Cannot restore Session context: SessionManager not available");
      }
      // 返回一个控制器，用于在上下文结束时清理
      return () -> {
        log.debug("Ending Session context propagation");
        // 清理操作（如果需要）
      };
    };
  }

  /**
   * 获取清理后的上下文（空上下文）
   * 在某些场景下，可能需要清除上下文
   */
  @Override
  public ThreadContextSnapshot clearedContext(Map<String, String> props) {
    log.debug("Providing cleared Session context");
    return () -> {
      // 清理上下文，不做任何操作
      log.debug("Session context cleared");
      // 返回一个空的控制器（无操作）
      return () -> {};
    };
  }
}

