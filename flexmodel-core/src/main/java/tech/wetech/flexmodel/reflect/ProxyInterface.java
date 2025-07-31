package tech.wetech.flexmodel.reflect;

import tech.wetech.flexmodel.EntityDefinition;

/**
 * @author cjbi
 */
public interface ProxyInterface {

  EntityDefinition entityInfo();

  Class<?> originClass();

}
