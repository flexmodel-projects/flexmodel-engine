package tech.wetech.flexmodel.codegen;

import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.parser.impl.ParseException;

/**
 * @author cjbi
 */
class GenerationToolTest extends AbstractIntegrationTest {

  @Test
  void test() {
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

}
