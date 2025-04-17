package tech.wetech.flexmodel.codegen;

import groovy.text.GStringTemplateEngine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.Entity;
import tech.wetech.flexmodel.ImportDescribe;
import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

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
    generationContext.getModelClassList().add(GenerationTool.buildModelClass(packageName, schemaName, (Entity) describe.getSchema().getFirst()));
    while (generationContext.nextModel()) {
      String str = daoGenerator.generate(generationContext);
      Assertions.assertNotNull(str);
    }
  }

  @Test
  void testGenerateModelList() throws Exception {
    String packageName = "com.example";
    String schemaName = "system";
    InputStream is = GeneratorTest.class.getClassLoader().getResourceAsStream("import.json");
    assert is != null;
    String content = new String(is.readAllBytes());
    ImportDescribe describe = new JacksonObjectConverter().parseToObject(content, ImportDescribe.class);
    GenerationContext generationContext = new GenerationContext();
    generationContext.getModelClassList().add(GenerationTool.buildModelClass(packageName, schemaName, (Entity) describe.getSchema().getFirst()));
    GStringTemplateEngine engine = new GStringTemplateEngine();
    URL resource = this.getClass().getClassLoader().getResource("templates/${packageName}/${modelClass.modelName}.java.template");
    while (generationContext.nextModel()) {
      String string = engine.createTemplate(resource).make(new JacksonObjectConverter().convertValue(generationContext, Map.class)).toString();
      System.out.println(string);
    }

//    def engine = new SimpleTemplateEngine()
//    def string = engine.createTemplate(templateText).make(name: "James", age: 20).toString()
  }

}
