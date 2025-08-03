package tech.wetech.flexmodel.cache;

import tech.wetech.flexmodel.ModelRepository;
import tech.wetech.flexmodel.model.SchemaObject;
import tech.wetech.flexmodel.session.AbstractSessionContext;

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
    List<SchemaObject> result = new ArrayList<>();

    // 从内存映射中获取
    Map<String, SchemaObject> schemaMap = map.get(schemaName);
    if (schemaMap != null) {
      result.addAll(schemaMap.values());
    }

    return result;
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
    // 从内存映射中查找
    return map.getOrDefault(schemaName, Collections.emptyMap()).get(modelName);
  }

}
