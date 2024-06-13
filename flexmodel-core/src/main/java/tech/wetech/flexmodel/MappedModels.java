package tech.wetech.flexmodel;


import java.util.List;

/**
 * @author cjbi
 */
public interface MappedModels {

  List<Model> sync(AbstractSessionContext sqlContext);

  List<Model> lookup(String schemaName);

  void removeAll(String schemaName);

  void remove(String schemaName, String modelName);

  void persist(String schemaName, Model model);

  Model getModel(String schemaName, String modelName);

}
