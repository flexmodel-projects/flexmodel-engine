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
    if (this.generatedValue.generator() != null) {
      this.calculators().remove(this.generatedValue.generator());
    }
    if (generatedValue.generator() != null) {
      this.addCalculation((ValueCalculator) generatedValue.generator());
    }
    this.generatedValue = generatedValue;
    return this;
  }

  public GeneratedValue generatedValue() {
    return generatedValue;
  }

  public IDField(String name) {
    super(name, BasicFieldType.ID.getType());
  }

  public enum DefaultGeneratedValue implements GeneratedValue {
    /**
     * 自增ID
     */
    IDENTITY("bigint", "identity", null),
    /**
     * UUID
     */
    UUID("string", "uuid", UUIDValueCalculator.INSTANCE),
    ;

    private final String type;
    private final String strategy;
    private final ValueCalculator<?> generator;

    DefaultGeneratedValue(String type, String strategy, ValueCalculator<?> generator) {
      this.type = type;
      this.strategy = strategy;
      this.generator = generator;
    }

    @Override
    public String type() {
      return type;
    }

    @Override
    public String strategy() {
      return strategy;
    }

    @Override
    public ValueCalculator<?> generator() {
      return generator;
    }
  }

  public interface GeneratedValue {

    String type();

    String strategy();

    ValueCalculator<?> generator();

  }

}
