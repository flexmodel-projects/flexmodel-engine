package tech.wetech.flexmodel.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.BuildItem;
import tech.wetech.flexmodel.ImportDescribe;
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
    log.info("开始从BuildItem加载脚本到内存...");
    ServiceLoader.load(BuildItem.class).forEach(this::loadScriptFromBuildItem);
    log.info("BuildItem脚本加载完成，共加载 {} 个schema", schemaScripts.size());
  }

  /**
   * 从单个BuildItem加载脚本
   */
  public void loadScriptFromBuildItem(BuildItem buildItem) {
    String schemaName = buildItem.getSchemaName();
    List<SchemaObject> schema = buildItem.getSchema();
    List<ImportDescribe.ImportData> data = buildItem.getData();

    schemaScripts.put(schemaName, new SchemaScriptConfig(schemaName, schema, data));
    log.info("加载schema: {}, 模型数量: {}, 数据项数量: {}", schemaName, schema.size(), data.size());
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
   * 检查schema是否有脚本配置
   */
  public boolean hasScriptConfig(String schemaName) {
    return schemaScripts.containsKey(schemaName);
  }

  /**
   * 脚本配置类
   */
  public static class SchemaScriptConfig {
    private final String schemaName;
    private final List<SchemaObject> schema;
    private final List<ImportDescribe.ImportData> data;

    public SchemaScriptConfig(String schemaName, List<SchemaObject> schema, List<ImportDescribe.ImportData> data) {
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

    public List<ImportDescribe.ImportData> getData() {
      return new ArrayList<>(data);
    }
  }
}
