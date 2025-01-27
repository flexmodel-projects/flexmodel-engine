package tech.wetech.flexmodel.graphql;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.Entity;
import tech.wetech.flexmodel.ImportDescribe;
import tech.wetech.flexmodel.TypeWrapper;
import tech.wetech.flexmodel.codegen.GenerationTool;
import tech.wetech.flexmodel.codegen.ModelListClass;
import tech.wetech.flexmodel.codegen.ModelListGenerationContext;
import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author cjbi
 */

class GraphQLSchemaGeneratorTest {

  @Test
  void generate() throws IOException {
    String packageName = "com.example";
    String schemaName = "system";
    InputStream is = GraphQLSchemaGeneratorTest.class.getClassLoader().getResourceAsStream("import.json");
    assert is != null;
    String content = new String(is.readAllBytes());
    ImportDescribe describe = new JacksonObjectConverter().parseToObject(content, ImportDescribe.class);
    GraphQLSchemaGenerator daoGenerator = new GraphQLSchemaGenerator();
    ModelListGenerationContext generationContext = new ModelListGenerationContext();
    ModelListClass modelListClass = new ModelListClass();
    modelListClass.setPackageName(packageName);
    modelListClass.setSchemaName(schemaName);
    for (TypeWrapper model : describe.getSchema()) {
      if (model instanceof Entity) {
        modelListClass.getModelList().add(GenerationTool.buildModelClass(packageName, schemaName, (Entity) model));
      }
    }
    generationContext.setModelListClass(modelListClass);
    String str = daoGenerator.generate(generationContext);
    Assertions.assertNotNull(str);
  }

}
