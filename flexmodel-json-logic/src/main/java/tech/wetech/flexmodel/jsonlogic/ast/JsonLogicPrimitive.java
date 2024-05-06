package tech.wetech.flexmodel.jsonlogic.ast;

/**
 * @author cjbi
 * @date 2022/9/4
 */
public interface JsonLogicPrimitive<T> extends JsonLogicNode {
  T getValue();

  JsonLogicPrimitiveType getPrimitiveType();

  @Override
  default JsonLogicNodeType getType() {
    return JsonLogicNodeType.PRIMITIVE;
  }
}
