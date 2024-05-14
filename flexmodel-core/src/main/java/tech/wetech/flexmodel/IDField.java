package tech.wetech.flexmodel;

import tech.wetech.flexmodel.calculations.UUIDValueCalculator;
import tech.wetech.flexmodel.calculations.ValueCalculator;

/**
 * ID字段类型默认为字符串，当为自增时则为数字，为UUID时则为字符串
 *
 * @author cjbi
 */
public class IDField extends TypedField<Object, IDField> {

  private GeneratedValue generatedValue = DefaultGeneratedValue.IDENTITY;

  @SuppressWarnings({"unchecked", "rawtypes"})
  public IDField setGeneratedValue(GeneratedValue generatedValue) {
    if (this.generatedValue.getGenerator() != null) {
      this.getCalculators().remove(this.generatedValue.getGenerator());
    }
    if (generatedValue.getGenerator() != null) {
      this.addCalculation((ValueCalculator) generatedValue.getGenerator());
    }
    this.generatedValue = generatedValue;
    return this;
  }

  public GeneratedValue getGeneratedValue() {
    return generatedValue;
  }

  public IDField(String name) {
    super(name, BasicFieldType.ID.getType());
  }

  public enum DefaultGeneratedValue implements GeneratedValue {
    /**
     * 自增ID
     */
    IDENTITY("bigint", null),
    /**
     * UUID
     */
    UUID("string", UUIDValueCalculator.INSTANCE),
    /**
     * 长整型不自动生成
     */
    BIGINT_NO_GEN("bigint", null),
    /**
     * 字符串不自动生成
     */
    STRING_NO_GEN("string", null),
    ;
    private final String type;
    private final ValueCalculator<?> generator;

    DefaultGeneratedValue(String type, ValueCalculator<?> generator) {
      this.type = type;
      this.generator = generator;
    }

    @Override
    public String getType() {
      return type;
    }

    @Override
    public ValueCalculator<?> getGenerator() {
      return generator;
    }
  }

  public interface GeneratedValue {

    String getType();

    ValueCalculator<?> getGenerator();

  }

}
