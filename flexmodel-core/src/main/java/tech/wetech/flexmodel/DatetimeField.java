package tech.wetech.flexmodel;

import java.time.LocalDateTime;

/**
 * @author cjbi
 */
public class DatetimeField extends TypedField<LocalDateTime, DatetimeField> {

  private GeneratedValue generatedValue;

  public DatetimeField(String name) {
    super(name, ScalarType.DATETIME.getType());
  }

  public GeneratedValue getGeneratedValue() {
    return generatedValue;
  }

  public DatetimeField setGeneratedValue(GeneratedValue generatedValue) {
    this.generatedValue = generatedValue;
    return this;
  }

  public enum GeneratedValue {
    NOW_ON_CREATE,
    NOW_ON_UPDATE,
    NOW_ON_CREATE_AND_UPDATE
  }

}
