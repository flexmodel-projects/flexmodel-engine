package tech.wetech.flexmodel;


import java.util.List;

/**
 * @author cjbi
 */
public interface MappedModels {

  List<Model> sync(AbstractSessionContext sqlContext);

  List<Model> lookup(String schemaName);

  void remove(String schemaName, String modelName);

  void persist(String schemaName, Model model);

  Model getModel(String schemaName, String modelName);

  default Entity getEntity(String schemaName, String modelName) {
    Model model = getModel(schemaName, modelName);
    if (model instanceof Entity entity) {
      return entity;
    }
    throw new IllegalArgumentException(String.format("Entity '%s.%s' doesn't exist", schemaName, modelName));
  }

}
