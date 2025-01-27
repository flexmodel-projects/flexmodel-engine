package tech.wetech.flexmodel;


import java.util.List;
import java.util.Set;

/**
 * @author cjbi
 */
public interface MappedModels {

  List<TypeWrapper> sync(AbstractSessionContext sqlContext);

  List<TypeWrapper> sync(AbstractSessionContext sqlContext, Set<String> includes);

  List<TypeWrapper> lookup(String schemaName);

  void removeAll(String schemaName);

  void remove(String schemaName, String modelName);

  void persist(String schemaName, TypeWrapper wrapper);

  TypeWrapper getModel(String schemaName, String modelName);

}
