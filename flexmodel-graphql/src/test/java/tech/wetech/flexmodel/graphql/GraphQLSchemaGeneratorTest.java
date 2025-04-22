package tech.wetech.flexmodel.graphql;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.Entity;
import tech.wetech.flexmodel.Enum;
import tech.wetech.flexmodel.ImportDescribe;
import tech.wetech.flexmodel.SchemaObject;
import tech.wetech.flexmodel.codegen.EnumClass;
import tech.wetech.flexmodel.codegen.GenerationContext;
import tech.wetech.flexmodel.codegen.ModelClass;
import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter;

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
    ImportDescribe describe = new JacksonObjectConverter().parseToObject(content, ImportDescribe.class);
    GraphQLSchemaGenerator daoGenerator = new GraphQLSchemaGenerator();
    GenerationContext generationContext = new GenerationContext();
    generationContext.setPackageName(packageName);
    generationContext.setSchemaName(schemaName);
    for (SchemaObject model : describe.getSchema()) {
      if (model instanceof Entity) {
        generationContext.getModelClassList().add(ModelClass.buildModelClass(packageName, schemaName, (Entity) model));
      } else if (model instanceof Enum anEnum) {
        generationContext.getEnumClassList().add(EnumClass.buildEnumClass(packageName, schemaName, anEnum));
      }
    }
    String str = daoGenerator.generate(generationContext).getFirst();
    Assertions.assertNotNull(str);
  }

}
