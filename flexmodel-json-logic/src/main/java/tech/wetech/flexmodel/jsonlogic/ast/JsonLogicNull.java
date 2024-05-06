package tech.wetech.flexmodel.jsonlogic.ast;

/**
 * @author cjbi
 * @date 2022/9/4
 */
public class JsonLogicNull implements JsonLogicPrimitive<Object> {
  public static final JsonLogicNull NULL = new JsonLogicNull();

  private JsonLogicNull() {
    // Consumers should use the NULL static instance.
  }

  @Override
  public Object getValue() {
    return null;
  }

  @Override
  public JsonLogicPrimitiveType getPrimitiveType() {
    return JsonLogicPrimitiveType.NULL;
  }
}
