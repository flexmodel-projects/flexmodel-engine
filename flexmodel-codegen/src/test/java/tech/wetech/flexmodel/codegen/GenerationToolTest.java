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
    Configuration.Connect dsConfig = new Configuration.Connect();
    dsConfig.setUrl("jdbc:sqlite:file::memory:?cache=shared");
    dsConfig.setDbKind("sqlite");
    schemaConfig.setConnect(dsConfig);
    configuration.setSchema(schemaConfig);
    Configuration.Target target = new Configuration.Target();
    target.setDirectory("src/test/java");
    target.setPackageName("com.example");
    configuration.setTarget(target);
    GenerationTool.run(configuration);
  }

}
