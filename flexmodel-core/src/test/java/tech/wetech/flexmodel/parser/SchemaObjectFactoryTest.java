package tech.wetech.flexmodel.parser;

import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.SchemaObject;
import tech.wetech.flexmodel.parser.impl.ModelParser;
import tech.wetech.flexmodel.parser.impl.ParseException;
import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cjbi
 */
public class SchemaObjectFactoryTest {

  @Test
  void test() throws ParseException {
    InputStream is = this.getClass().getClassLoader().getResourceAsStream("sample_input.sdl");
    ModelParser modelParser = new ModelParser(is);
    List<ModelParser.ASTNode> list = modelParser.CompilationUnit();
    List<SchemaObject> objectList = new ArrayList<>();
    for (ModelParser.ASTNode astNode : list) {
      objectList.add(ASTNodeConverter.toSchemaObject(astNode));
    }
    System.out.println(new JacksonObjectConverter().toJsonString(objectList));
    List<ModelParser.ASTNode> astNodeList = new ArrayList<>();
    for (SchemaObject schemaObject : objectList) {
      astNodeList.add(ASTNodeConverter.fromSchemaObject(schemaObject));
    }
    System.out.println(astNodeList);
  }

}
