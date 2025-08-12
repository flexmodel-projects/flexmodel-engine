package tech.wetech.flexmodel.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.BuildItem;
import tech.wetech.flexmodel.ModelImportBundle;
import tech.wetech.flexmodel.model.SchemaObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存脚本管理器
 * 用于管理启动时的配置脚本，避免每次启动都从数据库加载
 *
 * @author cjbi
 */
public class MemoryScriptManager {

  private static final Logger log = LoggerFactory.getLogger(MemoryScriptManager.class);

  // 存储每个schema的脚本配置
  private final Map<String, SchemaScriptConfig> schemaScripts = new ConcurrentHashMap<>();

  /**
   * 从BuildItem加载脚本到内存
   */
  public void loadScriptsFromBuildItems() {
    try {
      log.info("Start loading scripts from BuildItem to memory...");
      ServiceLoader.load(BuildItem.class).forEach(this::loadScriptFromBuildItem);
      log.info("The BuildItem script is loaded, loading a total of {} schemas", schemaScripts.size());
    } catch (Error e) {
      log.error("From BuildItem loading script to memory error: ", e);
    }
  }

  /**
   * 从单个BuildItem加载脚本
   */
  public void loadScriptFromBuildItem(BuildItem buildItem) {
    String schemaName = buildItem.getSchemaName();
    List<SchemaObject> schema = buildItem.getSchema();
    List<ModelImportBundle.ImportData> data = buildItem.getData();

    schemaScripts.put(schemaName, new SchemaScriptConfig(schemaName, schema, data));
    log.info("Load schema: {}, Number of models: {}, Number of data items: {}", schemaName, schema.size(), data.size());
  }

  /**
   * 获取指定schema的脚本配置
   */
  public SchemaScriptConfig getScriptConfig(String schemaName) {
    return schemaScripts.get(schemaName);
  }

  /**
   * 获取所有schema名称
   */
  public Set<String> getSchemaNames() {
    return schemaScripts.keySet();
  }

  /**
   * 脚本配置类
   */
  public static class SchemaScriptConfig {
    private final String schemaName;
    private final List<SchemaObject> schema;
    private final List<ModelImportBundle.ImportData> data;

    public SchemaScriptConfig(String schemaName, List<SchemaObject> schema, List<ModelImportBundle.ImportData> data) {
      this.schemaName = schemaName;
      this.schema = new ArrayList<>(schema);
      this.data = new ArrayList<>(data);
    }

    public String getSchemaName() {
      return schemaName;
    }

    public List<SchemaObject> getSchema() {
      return new ArrayList<>(schema);
    }

    public List<ModelImportBundle.ImportData> getData() {
      return new ArrayList<>(data);
    }
  }
}
