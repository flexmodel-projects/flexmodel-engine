package tech.wetech.flexmodel;

import tech.wetech.flexmodel.generator.ULIDValueGenerator;
import tech.wetech.flexmodel.generator.UUIDValueGenerator;
import tech.wetech.flexmodel.generator.ValueGenerator;

import static tech.wetech.flexmodel.generator.GenerationTime.INSERT;

/**
 * ID字段类型默认为字符串，当为自增时则为数字，为UUID时则为字符串
 *
 * @author cjbi
 */
public class IDField extends TypedField<Object, IDField> {

  private GeneratedValue generatedValue = GeneratedValue.AUTO_INCREMENT;

  @SuppressWarnings({"unchecked", "rawtypes"})
  public IDField setGeneratedValue(GeneratedValue generatedValue) {
    this.generatedValue = generatedValue;
    this.setGenerator((ValueGenerator) generatedValue.getGenerator());
    return this;
  }

  public GeneratedValue getGeneratedValue() {
    return generatedValue;
  }

  public IDField(String name) {
    super(name, ScalarType.ID.getType());
  }

  public enum GeneratedValue {
    /**
     * 自增ID
     */
    AUTO_INCREMENT("bigint", null),
    /**
     * UUID
     */
    UUID("string", UUIDValueGenerator.INSTANCE.setGenerationTime(INSERT)),
    /**
     * ULID
     */
    ULID("string", ULIDValueGenerator.INSTANCE.setGenerationTime(INSERT)),
    /**
     * 长整型不自动生成
     */
    BIGINT_NOT_GENERATED("bigint", null),
    /**
     * 字符串不自动生成
     */
    STRING_NOT_GENERATED("string", null),
    ;
    private final String type;
    private final ValueGenerator<?> generator;

    GeneratedValue(String type, ValueGenerator<?> generator) {
      this.type = type;
      this.generator = generator;
    }

    public String getType() {
      return type;
    }

    public ValueGenerator<?> getGenerator() {
      return generator;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof IDField idField)) return false;
    if (!super.equals(o)) return false;

      return getGeneratedValue() == idField.getGeneratedValue();
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (getGeneratedValue() != null ? getGeneratedValue().hashCode() : 0);
    return result;
  }
}
