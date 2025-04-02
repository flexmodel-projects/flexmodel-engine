package tech.wetech.flexmodel;

import tech.wetech.flexmodel.parser.ASTNodeConverter;
import tech.wetech.flexmodel.parser.impl.ModelParser;

import java.io.Serializable;

/**
 * @author cjbi
 */
public interface SchemaObject extends Serializable {

  /**
   * 名称
   *
   * @return
   */
  String getName();

  /**
   * 类型
   *
   * @return
   */
  String getType();

  default String getSdl() {
    ModelParser.ASTNode astNode = ASTNodeConverter.fromSchemaObject(this);
    if (astNode == null) {
      return null;
    }
    return astNode.toString();
  }
}
