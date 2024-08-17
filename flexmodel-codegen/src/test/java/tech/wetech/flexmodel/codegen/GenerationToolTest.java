package tech.wetech.flexmodel.codegen;

import org.junit.jupiter.api.Test;

/**
 * @author cjbi
 */
class GenerationToolTest extends AbstractIntegrationTest {

  @Test
  void test() {
    Configuration configuration = new Configuration();
    Configuration.SchemaConfig schemaConfig = new Configuration.SchemaConfig();
    schemaConfig.setSchemaName("system");
    Configuration.DSConfig dsConfig = new Configuration.DSConfig();
    dsConfig.setUrl("jdbc:sqlite:file::memory:?cache=shared");
    dsConfig.setDbKind("sqlite");
    schemaConfig.setDsConfig(dsConfig);
    configuration.setSchema(schemaConfig);
    Configuration.Target target = new Configuration.Target();
    target.setDirector("src/main/java");
    target.setPackageName("com.example");
    configuration.setTarget(target);
    GenerationTool.run(configuration);
  }

}
