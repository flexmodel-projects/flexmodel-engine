package tech.wetech.flexmodel.lazy;

import tech.wetech.flexmodel.Entity;

/**
 * @author cjbi
 */
public interface ProxyInterface {

  Entity entityInfo();

  Class<?> originClass();

}
