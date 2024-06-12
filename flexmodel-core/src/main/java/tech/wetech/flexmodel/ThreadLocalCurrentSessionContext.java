package tech.wetech.flexmodel;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cjbi
 */
public class ThreadLocalCurrentSessionContext implements CurrentSessionContext {

  private final SessionFactory sessionFactory;
  public static final ThreadLocal<Map<String, Session>> CONTEXT = ThreadLocal.withInitial(HashMap::new);

  public ThreadLocalCurrentSessionContext(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public Session currentSession(String identifier) {
    Map<String, Session> sessionMap = CONTEXT.get();
    return sessionMap.compute(identifier, (key, value) ->
      value == null ? this.sessionFactory.createSession(identifier) : value
    );
  }

  @Override
  public void destroy() {
    Map<String, Session> sessionMap = CONTEXT.get();
    for (Session session : sessionMap.values()) {
      try {
        session.close();
      } catch (Exception ignored) {

      }
    }
    sessionMap.clear();
  }

}
