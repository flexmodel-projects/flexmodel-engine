package tech.wetech.flexmodel.core.cache;

import tech.wetech.flexmodel.core.ModelRepository;
import tech.wetech.flexmodel.core.model.SchemaObject;
import tech.wetech.flexmodel.core.session.AbstractSessionContext;

import java.util.*;

/**
 * @author cjbi
 */
public class InMemoryModelRepository implements ModelRepository {

  private final Map<String, Map<String, SchemaObject>> map = new HashMap<>();

  @Override
  public List<SchemaObject> syncFromDatabase(AbstractSessionContext context) {
    // ignore
    return Collections.emptyList();
  }

  @Override
  public List<SchemaObject> syncFromDatabase(AbstractSessionContext sqlContext, Set<String> includes) {
    return Collections.emptyList();
  }

  @Override
  public List<SchemaObject> findAll(String schemaName) {
    return map.get(schemaName).values().stream().toList();
  }

  @Override
  public void deleteAll(String schemaName) {
    map.clear();
  }

  @Override
  public void delete(String schemaName, String modelName) {
    map.get(schemaName).remove(modelName);
  }

  @Override
  public void save(String schemaName, SchemaObject object) {
    map.compute(schemaName, (key, value) -> {
      if (value == null) {
        value = new HashMap<>();
      }
      value.put(object.getName(), object);
      return value;
    });
  }

  @Override
  public SchemaObject find(String schemaName, String modelName) {
    return map.getOrDefault(schemaName, Collections.emptyMap()).get(modelName);
  }

}
