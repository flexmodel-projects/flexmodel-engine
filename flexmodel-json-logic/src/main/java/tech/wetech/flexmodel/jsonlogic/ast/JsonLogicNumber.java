package tech.wetech.flexmodel.jsonlogic.ast;

/**
 * @author cjbi
 * @date 2022/9/4
 */
public class JsonLogicNumber implements JsonLogicPrimitive<Double> {
  private final Number value;

  public JsonLogicNumber(Number value) {
    this.value = value;
  }

  @Override
  public Double getValue() {
    return value.doubleValue();
  }

  @Override
  public JsonLogicPrimitiveType getPrimitiveType() {
    return JsonLogicPrimitiveType.NUMBER;
  }
}
