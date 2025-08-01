package tech.wetech.flexmodel.core;

import tech.wetech.flexmodel.core.model.SchemaObject;
import tech.wetech.flexmodel.core.session.AbstractSessionContext;

import java.util.*;

/**
 * @author cjbi
 */
public class MapMappedModels implements MappedModels {

  private final Map<String, Map<String, SchemaObject>> map = new HashMap<>();

  @Override
  public List<SchemaObject> sync(AbstractSessionContext context) {
    // ignore
    return Collections.emptyList();
  }

  @Override
  public List<SchemaObject> sync(AbstractSessionContext sqlContext, Set<String> includes) {
    return Collections.emptyList();
  }

  @Override
  public List<SchemaObject> lookup(String schemaName) {
    return map.get(schemaName).values().stream().toList();
  }

  @Override
  public void removeAll(String schemaName) {
    map.clear();
  }

  @Override
  public void remove(String schemaName, String modelName) {
    map.get(schemaName).remove(modelName);
  }

  @Override
  public void persist(String schemaName, SchemaObject object) {
    map.compute(schemaName, (key, value) -> {
      if (value == null) {
        value = new HashMap<>();
      }
      value.put(object.getName(), object);
      return value;
    });
  }

  @Override
  public SchemaObject getModel(String schemaName, String modelName) {
    return map.getOrDefault(schemaName, Collections.emptyMap()).get(modelName);
  }

}
