package tech.wetech.flexmodel.cache;

import tech.wetech.flexmodel.ModelRepository;
import tech.wetech.flexmodel.model.SchemaObject;
import tech.wetech.flexmodel.session.AbstractSessionContext;
import tech.wetech.flexmodel.session.MemoryScriptManager;

import java.util.*;

/**
 * @author cjbi
 */
public class InMemoryModelRepository implements ModelRepository {

  private final Map<String, Map<String, SchemaObject>> map = new HashMap<>();
  private MemoryScriptManager memoryScriptManager;

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

    // 从内存脚本管理器中获取
    if (memoryScriptManager != null && memoryScriptManager.hasScriptConfig(schemaName)) {
      MemoryScriptManager.SchemaScriptConfig config = memoryScriptManager.getScriptConfig(schemaName);
      result.addAll(config.getSchema());
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
    SchemaObject result = map.getOrDefault(schemaName, Collections.emptyMap()).get(modelName);
    if (result != null) {
      return result;
    }

    // 从内存脚本管理器中查找
    if (memoryScriptManager != null && memoryScriptManager.hasScriptConfig(schemaName)) {
      MemoryScriptManager.SchemaScriptConfig config = memoryScriptManager.getScriptConfig(schemaName);
      return config.getSchema().stream()
        .filter(model -> model.getName().equals(modelName))
        .findFirst()
        .orElse(null);
    }

    return null;
  }

  /**
   * 设置内存脚本管理器
   */
  public void setMemoryScriptManager(MemoryScriptManager memoryScriptManager) {
    this.memoryScriptManager = memoryScriptManager;
  }

}
