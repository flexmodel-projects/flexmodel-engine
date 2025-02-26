package tech.wetech.flexmodel.reflect;

import tech.wetech.flexmodel.Entity;

/**
 * @author cjbi
 */
public interface ProxyInterface {

  Entity entityInfo();

  Class<?> originClass();

}
