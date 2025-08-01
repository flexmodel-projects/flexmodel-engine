package tech.wetech.flexmodel.core.parser;

import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.core.parser.impl.ModelParser;
import tech.wetech.flexmodel.core.parser.impl.ParseException;

import java.io.InputStream;
import java.util.List;

/**
 * @author cjbi
 */
public class ModelParserTest {

  @Test
  void test() throws ParseException {
    InputStream is = this.getClass().getClassLoader().getResourceAsStream("sample_input.idl");
    // 从标准输入解析，解析结果为 AST 节点列表
    ModelParser parser = new ModelParser(is);
    List ast = parser.CompilationUnit();
    System.out.println("Parsing successful.");
    // 遍历打印 AST
    for (Object node : ast) {
      System.out.println(node);
    }
  }

}
