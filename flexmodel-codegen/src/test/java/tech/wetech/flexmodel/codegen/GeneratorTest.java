package tech.wetech.flexmodel.codegen;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.Entity;
import tech.wetech.flexmodel.ImportDescribe;
import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author cjbi
 */
class GeneratorTest {

  @Test
  void testGenerate() throws IOException {
    String packageName = "com.example";
    String schemaName = "system";
    InputStream is = GeneratorTest.class.getClassLoader().getResourceAsStream("import.json");
    assert is != null;
    String content = new String(is.readAllBytes());
    ImportDescribe describe = new JacksonObjectConverter().parseToObject(content, ImportDescribe.class);
    DaoGenerator daoGenerator = new DaoGenerator();
    GenerationContext generationContext = new GenerationContext();
    generationContext.setModelClass(GenerationTool.buildModelClass(packageName, schemaName, (Entity) describe.getSchema().getFirst()));
    String str = daoGenerator.generate(generationContext);
    Assertions.assertNotNull(str);
  }

}
