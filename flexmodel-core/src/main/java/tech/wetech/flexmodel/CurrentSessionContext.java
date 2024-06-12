package tech.wetech.flexmodel;

/**
 * @author cjbi
 */
public interface CurrentSessionContext {

  Session currentSession(String identifier);

  void destroy();

}
