package tech.wetech.flexmodel;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
public class MapMappedModels implements MappedModels {

  private final Map<String, Map<String, Model>> map = new HashMap<>();

  @Override
  public List<Model> sync(AbstractSessionContext context) {
    // ignore
    return Collections.emptyList();
  }

  @Override
  public List<Model> lookup(String schemaName) {
    return map.get(schemaName).values().stream().toList();
  }

  @Override
  public void remove(String schemaName, String modelName) {
    map.get(schemaName).remove(modelName);
  }

  @Override
  public void persist(String schemaName, Model model) {
    map.compute(schemaName, (key, value) -> {
      if (value == null) {
        value = new HashMap<>();
      }
      value.put(model.getName(), model);
      return value;
    });
  }

  @Override
  public Model getModel(String schemaName, String modelName) {
    return map.getOrDefault(schemaName, Collections.emptyMap()).get(modelName);
  }

}
