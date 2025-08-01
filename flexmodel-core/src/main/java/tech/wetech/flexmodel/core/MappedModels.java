package tech.wetech.flexmodel.core;


import tech.wetech.flexmodel.core.session.AbstractSessionContext;

import java.util.List;
import java.util.Set;

/**
 * @author cjbi
 */
public interface MappedModels {

  List<SchemaObject> sync(AbstractSessionContext sqlContext);

  List<SchemaObject> sync(AbstractSessionContext sqlContext, Set<String> includes);

  List<SchemaObject> lookup(String schemaName);

  void removeAll(String schemaName);

  void remove(String schemaName, String modelName);

  void persist(String schemaName, SchemaObject object);

  SchemaObject getModel(String schemaName, String modelName);

}
