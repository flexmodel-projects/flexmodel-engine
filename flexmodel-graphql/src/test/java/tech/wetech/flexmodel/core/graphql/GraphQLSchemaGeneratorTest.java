package tech.wetech.flexmodel.core.graphql;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.codegen.EnumClass;
import tech.wetech.flexmodel.codegen.GenerationContext;
import tech.wetech.flexmodel.codegen.ModelClass;
import tech.wetech.flexmodel.core.ImportDescribe;
import tech.wetech.flexmodel.core.model.EntityDefinition;
import tech.wetech.flexmodel.core.model.EnumDefinition;
import tech.wetech.flexmodel.core.model.SchemaObject;
import tech.wetech.flexmodel.core.supports.jackson.JacksonObjectConverter;
import tech.wetech.flexmodel.graphql.GraphQLSchemaGenerator;

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
      if (model instanceof EntityDefinition) {
        generationContext.getModelClassList().add(ModelClass.buildModelClass(packageName, schemaName, (EntityDefinition) model));
      } else if (model instanceof EnumDefinition anEnum) {
        generationContext.getEnumClassList().add(EnumClass.buildEnumClass(packageName, schemaName, anEnum));
      }
    }
    String str = daoGenerator.generate(generationContext).getFirst();
    Assertions.assertNotNull(str);
  }

}
