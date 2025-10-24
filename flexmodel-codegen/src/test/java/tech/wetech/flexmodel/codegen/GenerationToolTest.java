package tech.wetech.flexmodel.codegen;

import org.junit.jupiter.api.Test;

import java.io.File;

/**
 * @author cjbi
 */
class GenerationToolTest extends AbstractIntegrationTest {

  @Test
  void test() throws Exception {
    Configuration configuration = new Configuration();
    SchemaConfig schemaConfig = new SchemaConfig();
    schemaConfig.setName("system");
    schemaConfig.setBaseDir("src/test/resources/");
    schemaConfig.setDirectory("src/test/java");
    schemaConfig.setPackageName("com.example");
    schemaConfig.setReplaceString("f_");
    configuration.addSchema(schemaConfig);

    GenerationTool.run(configuration);
  }

  @Test
  void testIDL() {
    Configuration configuration = new Configuration();
    SchemaConfig schemaConfig = new SchemaConfig();
    schemaConfig.setName("system_idl");
    schemaConfig.setImportScript("import.idl");
    schemaConfig.setBaseDir("src/test/resources/");
    schemaConfig.setDirectory("src/test/java");
    schemaConfig.setPackageName("com.example_idl");
    schemaConfig.setReplaceString("f_");
    configuration.addSchema(schemaConfig);
    GenerationTool.run(configuration);
  }
  public static void listFiles(File dir) {
    File[] files = dir.listFiles();
    if (files == null) return;

    for (File file : files) {
      if (file.isDirectory()) {
        System.out.println("dir: " + file.getAbsolutePath());
        listFiles(file); // 递归遍历子目录
      } else {
        System.out.println(file.getAbsolutePath()); // 打印文件路径
      }
    }
  }

}
