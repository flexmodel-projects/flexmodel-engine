package tech.wetech.flexmodel.core.reflect;

import tech.wetech.flexmodel.core.model.EntityDefinition;

/**
 * @author cjbi
 */
public interface ProxyInterface {

  EntityDefinition entityInfo();

  Class<?> originClass();

}
