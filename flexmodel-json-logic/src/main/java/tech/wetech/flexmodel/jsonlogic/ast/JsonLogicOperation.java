package tech.wetech.flexmodel.jsonlogic.ast;

/**
 * @author cjbi
 * @date 2022/9/4
 */
public class JsonLogicOperation implements JsonLogicNode {

  private final String operator;
  private final JsonLogicArray arguments;

  public JsonLogicOperation(String operator, JsonLogicArray arguments) {
    this.operator = operator;
    this.arguments = arguments;
  }

  @Override
  public JsonLogicNodeType getType() {
    return JsonLogicNodeType.OPERATION;
  }

  public String getOperator() {
    return operator;
  }

  public JsonLogicArray getArguments() {
    return arguments;
  }
}
