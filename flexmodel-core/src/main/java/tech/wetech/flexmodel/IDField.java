package tech.wetech.flexmodel;

import static tech.wetech.flexmodel.IDField.GeneratedValue.AUTO_INCREMENT;
import static tech.wetech.flexmodel.ScalarType.BIGINT;
import static tech.wetech.flexmodel.ScalarType.STRING;

/**
 * ID字段类型默认为字符串，当为自增时则为数字，为UUID时则为字符串
 *
 * @author cjbi
 */
public class IDField extends TypedField<Object, IDField> {

  private GeneratedValue generatedValue = AUTO_INCREMENT;

  public IDField(String name) {
    super(name, ScalarType.ID.getType());
  }

  public GeneratedValue getGeneratedValue() {
    return generatedValue;
  }

  public IDField setGeneratedValue(GeneratedValue generatedValue) {
    this.generatedValue = generatedValue;
    return this;
  }

  public enum GeneratedValue {
    /**
     * 自增ID
     */
    AUTO_INCREMENT(BIGINT.getType()),
    /**
     * UUID
     */
    UUID(STRING.getType()),
    /**
     * ULID
     */
    ULID(STRING.getType()),
    /**
     * 长整型不自动生成
     */
    BIGINT_NOT_GENERATED(BIGINT.getType()),
    /**
     * 字符串不自动生成
     */
    STRING_NOT_GENERATED(STRING.getType()),
    ;
    private final String type;

    GeneratedValue(String type) {
      this.type = type;
    }

    public String getType() {
      return type;
    }
  }

}
