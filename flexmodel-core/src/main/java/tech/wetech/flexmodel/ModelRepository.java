package tech.wetech.flexmodel;


import tech.wetech.flexmodel.model.SchemaObject;
import tech.wetech.flexmodel.session.AbstractSessionContext;

import java.util.List;
import java.util.Set;

/**
 * @author cjbi
 */
public interface ModelRepository {

  List<SchemaObject> syncFromDatabase(AbstractSessionContext sqlContext);

  List<SchemaObject> syncFromDatabase(AbstractSessionContext sqlContext, Set<String> includes);

  List<SchemaObject> findAll(String schemaName);

  void deleteAll(String schemaName);

  void delete(String schemaName, String modelName);

  void save(String schemaName, SchemaObject object);

  SchemaObject find(String schemaName, String modelName);

}
