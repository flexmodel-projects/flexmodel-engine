package tech.wetech.flexmodel.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.BuildItem;
import tech.wetech.flexmodel.ImportDescribe;
import tech.wetech.flexmodel.model.EntityDefinition;
import tech.wetech.flexmodel.model.SchemaObject;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 内存脚本管理器测试
 */
public class MemoryScriptManagerTest {

  private MemoryScriptManager memoryScriptManager;

  @BeforeEach
  void setUp() {
    memoryScriptManager = new MemoryScriptManager();
  }

  @Test
  void testLoadScriptsFromBuildItems() {
    // 创建测试BuildItem
    TestBuildItem testBuildItem = new TestBuildItem("testSchema", createTestSchema(), createTestData());

    // 模拟ServiceLoader加载
    memoryScriptManager.loadScriptFromBuildItem(testBuildItem);

    // 验证脚本已加载
    assertTrue(memoryScriptManager.hasScriptConfig("testSchema"));
    assertEquals(1, memoryScriptManager.getSchemaNames().size());
    assertTrue(memoryScriptManager.getSchemaNames().contains("testSchema"));

    // 验证脚本内容
    MemoryScriptManager.SchemaScriptConfig config = memoryScriptManager.getScriptConfig("testSchema");
    assertNotNull(config);
    assertEquals("testSchema", config.getSchemaName());
    assertEquals(1, config.getSchema().size());
    assertEquals(1, config.getData().size());
    assertEquals("TestEntity", config.getSchema().get(0).getName());
    assertEquals("TestEntity", config.getData().get(0).getModelName());
  }

  @Test
  void testGetScriptConfigForNonExistentSchema() {
    assertNull(memoryScriptManager.getScriptConfig("nonExistentSchema"));
    assertFalse(memoryScriptManager.hasScriptConfig("nonExistentSchema"));
  }

  @Test
  void testSchemaScriptConfig() {
    List<SchemaObject> schema = createTestSchema();
    List<ImportDescribe.ImportData> data = createTestData();

    MemoryScriptManager.SchemaScriptConfig config = new MemoryScriptManager.SchemaScriptConfig("testSchema", schema, data);

    assertEquals("testSchema", config.getSchemaName());
    assertEquals(1, config.getSchema().size());
    assertEquals(1, config.getData().size());

    // 验证返回的是副本，不是原始引用
    List<SchemaObject> schemaCopy = config.getSchema();
    List<ImportDescribe.ImportData> dataCopy = config.getData();

    assertNotSame(schema, schemaCopy);
    assertNotSame(data, dataCopy);
  }

  private List<SchemaObject> createTestSchema() {
    List<SchemaObject> schema = new ArrayList<>();
    EntityDefinition entity = new EntityDefinition("TestEntity");
    schema.add(entity);
    return schema;
  }

  private List<ImportDescribe.ImportData> createTestData() {
    List<ImportDescribe.ImportData> data = new ArrayList<>();
    ImportDescribe.ImportData importData = new ImportDescribe.ImportData();
    importData.setModelName("TestEntity");
    importData.setValues(new ArrayList<>());
    data.add(importData);
    return data;
  }

  /**
   * 测试用的BuildItem实现
   */
  private static class TestBuildItem implements BuildItem {
    private final String schemaName;
    private final List<SchemaObject> schema;
    private final List<ImportDescribe.ImportData> data;

    public TestBuildItem(String schemaName, List<SchemaObject> schema, List<ImportDescribe.ImportData> data) {
      this.schemaName = schemaName;
      this.schema = schema;
      this.data = data;
    }

    @Override
    public String getSchemaName() {
      return schemaName;
    }

    @Override
    public List<SchemaObject> getSchema() {
      return schema;
    }

    @Override
    public List<ImportDescribe.ImportData> getData() {
      return data;
    }
  }
}
