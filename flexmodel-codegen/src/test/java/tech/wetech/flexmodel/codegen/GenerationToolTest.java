package tech.wetech.flexmodel.codegen;

import org.junit.jupiter.api.Test;

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

}
