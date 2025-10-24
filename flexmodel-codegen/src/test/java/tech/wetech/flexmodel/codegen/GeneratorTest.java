package tech.wetech.flexmodel.codegen;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.ModelImportBundle;
import tech.wetech.flexmodel.model.EntityDefinition;
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
    ModelImportBundle describe = new JacksonObjectConverter().parseToObject(content, ModelImportBundle.class);
    PojoGenerator generator = new PojoGenerator();
    GenerationContext generationContext = new GenerationContext();
    generationContext.getModelClassList().add(ModelClass.buildModelClass(packageName, schemaName, (EntityDefinition) describe.getObjects().getFirst()));
    String str = generator.generate(generationContext).getFirst();
    Assertions.assertNotNull(str);
  }

}
