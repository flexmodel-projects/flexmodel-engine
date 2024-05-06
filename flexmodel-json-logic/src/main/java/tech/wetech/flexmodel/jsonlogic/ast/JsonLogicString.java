package tech.wetech.flexmodel.jsonlogic.ast;

/**
 * @author cjbi
 * @date 2022/9/4
 */
public class JsonLogicString implements JsonLogicPrimitive<String> {
  private final String value;

  public JsonLogicString(String value) {
    this.value = value;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public JsonLogicPrimitiveType getPrimitiveType() {
    return JsonLogicPrimitiveType.STRING;
  }
}
