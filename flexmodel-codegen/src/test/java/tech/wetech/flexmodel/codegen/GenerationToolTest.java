package tech.wetech.flexmodel.codegen;

import groovy.text.GStringTemplateEngine;
import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.Entity;
import tech.wetech.flexmodel.ImportDescribe;
import tech.wetech.flexmodel.parser.impl.ParseException;
import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Objects;

/**
 * @author cjbi
 */
class GenerationToolTest extends AbstractIntegrationTest {

  @Test
  void test() throws Exception {
    Configuration configuration = new Configuration();
    Configuration.Schema schemaConfig = new Configuration.Schema();
    schemaConfig.setName("system");
    configuration.setSchema(schemaConfig);
    Configuration.Target target = new Configuration.Target();
    target.setBaseDir("src/test/resources/");
    target.setDirectory("src/test/java");
    target.setPackageName("com.example");
    target.setReplaceString("fs_|fe_");
    configuration.setTarget(target);
    GenerationTool.run(configuration);
  }

  @Test
  void testV2() throws Exception {
    Configuration configuration = new Configuration();
    Configuration.Schema schemaConfig = new Configuration.Schema();
    schemaConfig.setName("system");
    configuration.setSchema(schemaConfig);
    Configuration.Target target = new Configuration.Target();
    target.setBaseDir("src/test/resources/");
    target.setDirectory("src/test/java");
    target.setPackageName("com.example");
    target.setReplaceString("fs_|fe_");
    configuration.setTarget(target);
    GenerationTool.runV2(configuration);
  }

  @Test
  void testIDL() throws ParseException {
    Configuration configuration = new Configuration();
    Configuration.Schema schemaConfig = new Configuration.Schema();
    schemaConfig.setName("system_idl");
    schemaConfig.setImportScript("import.idl");
    configuration.setSchema(schemaConfig);
    Configuration.Target target = new Configuration.Target();
    target.setBaseDir("src/test/resources/");
    target.setDirectory("src/test/java");
    target.setPackageName("com.example_idl");
    target.setReplaceString("fs_|fe_");
    configuration.setTarget(target);
    GenerationTool.run(configuration);
  }

//  @Test
  void testGenerateModelList() throws Exception {
    String packageName = "com.example";
    String schemaName = "system";
    InputStream is = GeneratorTest.class.getClassLoader().getResourceAsStream("import.json");
    assert is != null;
    String content = new String(is.readAllBytes());
    ImportDescribe describe = new JacksonObjectConverter().parseToObject(content, ImportDescribe.class);
    GenerationContext generationContext = new GenerationContext();
    generationContext.setPackageName(packageName);
    generationContext.getModelClassList().add(GenerationTool.buildModelClass(packageName, schemaName, (Entity) describe.getSchema().getFirst()));
    GStringTemplateEngine engine = new GStringTemplateEngine();
    URL resource = this.getClass().getClassLoader().getResource("templates/${packageNameAsPath}/${modelClass.modelName}.java.template");
    while (generationContext.nextModel()) {
      String string = engine.createTemplate(resource).make(new JacksonObjectConverter().convertValue(generationContext, Map.class)).toString();
      System.out.println(string);
    }
    listFiles(new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("templates/")).toURI()));
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
