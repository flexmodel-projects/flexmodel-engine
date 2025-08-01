package tech.wetech.flexmodel.core.model;

import tech.wetech.flexmodel.core.parser.ASTNodeConverter;
import tech.wetech.flexmodel.core.parser.impl.ModelParser;

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

  default String getIdl() {
    ModelParser.ASTNode astNode = ASTNodeConverter.fromSchemaObject(this);
    if (astNode == null) {
      return null;
    }
    return astNode.toString();
  }
}
