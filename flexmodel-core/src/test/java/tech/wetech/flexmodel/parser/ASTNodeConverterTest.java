package tech.wetech.flexmodel.parser;

import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.SchemaObject;
import tech.wetech.flexmodel.parser.impl.ModelParser;
import tech.wetech.flexmodel.parser.impl.ParseException;
import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
public class ASTNodeConverterTest {

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

  @Test
  void test2() throws IOException {
    byte[] bytes = this.getClass().getClassLoader().getResourceAsStream("sample_input.json").readAllBytes();
    List<Map<String, Object>> list = new JacksonObjectConverter().parseToMapList(new String(bytes));
    List<SchemaObject> schemaObjects = new JacksonObjectConverter().convertValueList(list, SchemaObject.class);
    StringBuilder sb = new StringBuilder();
    for (SchemaObject schemaObject : schemaObjects) {
      sb.append(ASTNodeConverter.fromSchemaObject(schemaObject)).append("\n");
    }
    System.out.println(sb);
  }

}
