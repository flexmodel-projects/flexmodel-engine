package tech.wetech.flexmodel.quarkus.session;

import tech.wetech.flexmodel.session.Session;
import tech.wetech.flexmodel.session.SessionManager;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Session上下文
 * 用于在异步操作中传播Session状态
 * 
 * 由于Session实例本身可能包含不可序列化的资源（如数据库连接），
 * 这里只存储Session的标识符（schema名称），在实际使用时通过
 * SessionManager重新获取Session实例。
 *
 * @author cjbi
 */
public class SessionContext implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * 活跃的Session schema名称集合
   * 每个schema对应一个Session实例
   */
  private final Set<String> activeSchemas;

  /**
   * 最近使用的schema名称
   */
  private final String lastUsedSchema;

  /**
   * 私有构造函数
   */
  private SessionContext(Set<String> activeSchemas, String lastUsedSchema) {
    this.activeSchemas = activeSchemas;
    this.lastUsedSchema = lastUsedSchema;
  }

  /**
   * 从SessionManager创建SessionContext
   * 
   * @param sessionManager Session管理器
   * @return SessionContext实例，如果没有活跃的Session则返回null
   */
  public static SessionContext from(SessionManager sessionManager) {
    if (sessionManager == null || !sessionManager.hasActiveSession()) {
      return null;
    }

    // 获取所有活跃的schema名称
    // 注意：这里我们需要访问SessionManager的内部状态
    // 由于SessionManager使用ThreadLocal，我们需要通过反射或者提供一个公开方法
    // 这里我们先假设通过一种方式可以获取到这些信息
    
    // 为了简化，我们可以通过尝试获取已知的Session来判断
    // 但更好的方法是让SessionManager提供一个方法来获取所有活跃的schema
    Map<String, Session> sessionMap = getSessionMap(sessionManager);
    String lastUsedSchema = getLastUsedSchema(sessionManager);
    
    if (sessionMap == null || sessionMap.isEmpty()) {
      return null;
    }

    return new SessionContext(sessionMap.keySet(), lastUsedSchema);
  }

  /**
   * 将SessionContext恢复到SessionManager
   * 
   * @param sessionManager Session管理器
   * @param context SessionContext实例
   */
  public static void restoreTo(SessionManager sessionManager, SessionContext context) {
    if (sessionManager == null || context == null) {
      return;
    }

    // 恢复Session到SessionManager的ThreadLocal中
    // 注意：这里的Session实例需要重新从SessionManager获取，而不是直接使用旧的实例
    // 因为旧的Session实例可能绑定到旧的线程
    
    // 对于每个schema，通过SessionManager获取Session（如果不存在会创建）
    Map<String, Session> sessionMap = getOrCreateSessionMap(sessionManager);
    
    for (String schema : context.activeSchemas) {
      if (!sessionMap.containsKey(schema)) {
        // 重新获取Session（如果已存在则复用，不存在则创建）
        sessionManager.getSession(schema);
      }
    }

    // 恢复lastUsedSchema
    if (context.lastUsedSchema != null) {
      setLastUsedSchema(sessionManager, context.lastUsedSchema);
    }
  }

  /**
   * 获取活跃的schema集合
   */
  public Set<String> getActiveSchemas() {
    return activeSchemas;
  }

  /**
   * 获取最近使用的schema
   */
  public String getLastUsedSchema() {
    return lastUsedSchema;
  }

  /**
   * 检查是否为空
   */
  public boolean isEmpty() {
    return activeSchemas == null || activeSchemas.isEmpty();
  }

  /**
   * 通过反射或公开方法获取SessionMap
   * 这里使用反射访问protected字段，但更好的方法是在SessionManager中添加公开方法
   */
  @SuppressWarnings("unchecked")
  private static Map<String, Session> getSessionMap(SessionManager sessionManager) {
    try {
      java.lang.reflect.Field field = SessionManager.class.getDeclaredField("sessionMapHolder");
      field.setAccessible(true);
      ThreadLocal<Map<String, Session>> holder = (ThreadLocal<Map<String, Session>>) field.get(sessionManager);
      if (holder != null) {
        return holder.get();
      }
    } catch (Exception e) {
      // 如果反射失败，返回null
    }
    return null;
  }

  /**
   * 获取或创建SessionMap
   */
  @SuppressWarnings("unchecked")
  private static Map<String, Session> getOrCreateSessionMap(SessionManager sessionManager) {
    try {
      java.lang.reflect.Field field = SessionManager.class.getDeclaredField("sessionMapHolder");
      field.setAccessible(true);
      ThreadLocal<Map<String, Session>> holder = (ThreadLocal<Map<String, Session>>) field.get(sessionManager);
      if (holder == null) {
        return null;
      }
      Map<String, Session> sessionMap = holder.get();
      if (sessionMap == null) {
        sessionMap = new HashMap<>();
        holder.set(sessionMap);
      }
      return sessionMap;
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * 获取lastUsedSchema
   */
  private static String getLastUsedSchema(SessionManager sessionManager) {
    try {
      java.lang.reflect.Field field = SessionManager.class.getDeclaredField("lastUsedSchemaHolder");
      field.setAccessible(true);
      ThreadLocal<String> holder = (ThreadLocal<String>) field.get(sessionManager);
      if (holder != null) {
        return holder.get();
      }
    } catch (Exception e) {
      // 忽略
    }
    return null;
  }

  /**
   * 设置lastUsedSchema
   */
  private static void setLastUsedSchema(SessionManager sessionManager, String schema) {
    try {
      java.lang.reflect.Field field = SessionManager.class.getDeclaredField("lastUsedSchemaHolder");
      field.setAccessible(true);
      ThreadLocal<String> holder = (ThreadLocal<String>) field.get(sessionManager);
      if (holder != null) {
        holder.set(schema);
      }
    } catch (Exception e) {
      // 忽略
    }
  }
}

