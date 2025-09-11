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
    Schema schemaConfig = new Schema();
    schemaConfig.setName("system");
    configuration.setSchema(schemaConfig);
    Target target = new Target();
    target.setBaseDir("src/test/resources/");
    target.setDirectory("src/test/java");
    target.setPackageName("com.example");
    target.setReplaceString("fs_|fe_");
    configuration.setTarget(target);
    GenerationTool.run(configuration);
  }

  @Test
  void testIDL() {
    Configuration configuration = new Configuration();
    Schema schemaConfig = new Schema();
    schemaConfig.setName("system_idl");
    schemaConfig.setImportScript("import.idl");
    configuration.setSchema(schemaConfig);
    Target target = new Target();
    target.setBaseDir("src/test/resources/");
    target.setDirectory("src/test/java");
    target.setPackageName("com.example_idl");
    target.setReplaceString("fs_|fe_");
    configuration.setTarget(target);
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
