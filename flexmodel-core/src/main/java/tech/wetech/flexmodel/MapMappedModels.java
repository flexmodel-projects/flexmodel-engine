package tech.wetech.flexmodel;

import java.util.*;

/**
 * @author cjbi
 */
public class MapMappedModels implements MappedModels {

  private final Map<String, Map<String, TypeWrapper>> map = new HashMap<>();

  @Override
  public List<TypeWrapper> sync(AbstractSessionContext context) {
    // ignore
    return Collections.emptyList();
  }

  @Override
  public List<TypeWrapper> sync(AbstractSessionContext sqlContext, Set<String> includes) {
    return Collections.emptyList();
  }

  @Override
  public List<TypeWrapper> lookup(String schemaName) {
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
  public void persist(String schemaName, TypeWrapper wrapper) {
    map.compute(schemaName, (key, value) -> {
      if (value == null) {
        value = new HashMap<>();
      }
      value.put(wrapper.getName(), wrapper);
      return value;
    });
  }

  @Override
  public TypeWrapper getModel(String schemaName, String modelName) {
    return map.getOrDefault(schemaName, Collections.emptyMap()).get(modelName);
  }

}
