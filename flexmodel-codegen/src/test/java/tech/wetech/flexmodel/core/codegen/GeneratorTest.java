package tech.wetech.flexmodel.core.codegen;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.codegen.GenerationContext;
import tech.wetech.flexmodel.codegen.ModelClass;
import tech.wetech.flexmodel.core.ImportDescribe;
import tech.wetech.flexmodel.core.model.EntityDefinition;
import tech.wetech.flexmodel.core.supports.jackson.JacksonObjectConverter;

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
    DAOGenerator daoGenerator = new DAOGenerator();
    GenerationContext generationContext = new GenerationContext();
    generationContext.getModelClassList().add(ModelClass.buildModelClass(packageName, schemaName, (EntityDefinition) describe.getSchema().getFirst()));
    String str = daoGenerator.generate(generationContext).getFirst();
    Assertions.assertNotNull(str);
  }

}
