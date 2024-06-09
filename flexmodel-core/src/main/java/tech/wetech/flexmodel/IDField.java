package tech.wetech.flexmodel;

import tech.wetech.flexmodel.generations.ULIDValueGenerator;
import tech.wetech.flexmodel.generations.UUIDValueGenerator;
import tech.wetech.flexmodel.generations.ValueGenerator;

/**
 * ID字段类型默认为字符串，当为自增时则为数字，为UUID时则为字符串
 *
 * @author cjbi
 */
public class IDField extends TypedField<Object, IDField> {

  private GeneratedValue generatedValue = GeneratedValue.AUTO_INCREMENT;

  @SuppressWarnings({"unchecked", "rawtypes"})
  public IDField setGeneratedValue(GeneratedValue generatedValue) {
    if (this.generatedValue.getGenerator() != null) {
      this.getGenerators().remove(this.generatedValue.getGenerator());
    }
    if (generatedValue.getGenerator() != null) {
      this.addGenration((ValueGenerator) generatedValue.getGenerator());
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

  public enum GeneratedValue {
    /**
     * 自增ID
     */
    AUTO_INCREMENT("bigint", null),
    /**
     * UUID
     */
    UUID("string", UUIDValueGenerator.INSTANCE),
    /**
     * ULID
     */
    ULID("string", ULIDValueGenerator.INSTANCE),
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


}
