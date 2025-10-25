package tech.wetech.flexmodel.graphql;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.JsonUtils;
import tech.wetech.flexmodel.ModelImportBundle;
import tech.wetech.flexmodel.codegen.EnumClass;
import tech.wetech.flexmodel.codegen.GenerationContext;
import tech.wetech.flexmodel.codegen.ModelClass;
import tech.wetech.flexmodel.model.EntityDefinition;
import tech.wetech.flexmodel.model.EnumDefinition;
import tech.wetech.flexmodel.model.SchemaObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author cjbi
 */

class GraphQLSchemaGeneratorTest {

  @Test
  void write() throws IOException {
    String packageName = "com.example";
    String schemaName = "system";
    InputStream is = GraphQLSchemaGeneratorTest.class.getClassLoader().getResourceAsStream("import.json");
    assert is != null;
    String content = new String(is.readAllBytes());
    ModelImportBundle describe = JsonUtils.parseToObject(content, ModelImportBundle.class);
    GraphQLSchemaGenerator daoGenerator = new GraphQLSchemaGenerator();
    GenerationContext generationContext = new GenerationContext();
    generationContext.setPackageName(packageName);
    generationContext.setSchemaName(schemaName);
    for (SchemaObject schemaObject : describe.getObjects()) {
      if (schemaObject instanceof EntityDefinition) {
        generationContext.getModelClassList().add(ModelClass.buildModelClass(packageName, schemaName, (EntityDefinition) schemaObject));
      } else if (schemaObject instanceof EnumDefinition anEnum) {
        generationContext.getEnumClassList().add(EnumClass.buildEnumClass(packageName, schemaName, anEnum));
      }
    }
    String str = daoGenerator.generate(generationContext).getFirst();
    Assertions.assertNotNull(str);
  }

}
