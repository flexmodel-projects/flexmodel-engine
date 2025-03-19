package tech.wetech.flexmodel;

import java.time.LocalDateTime;

/**
 * @author cjbi
 */
public class DateTimeField extends TypedField<LocalDateTime, DateTimeField> {

  private GeneratedValue generatedValue;

  public DateTimeField(String name) {
    super(name, ScalarType.DATETIME.getType());
  }

  public GeneratedValue getGeneratedValue() {
    return generatedValue;
  }

  public DateTimeField setGeneratedValue(GeneratedValue generatedValue) {
    this.generatedValue = generatedValue;
    return this;
  }

  public enum GeneratedValue {
    NOW_ON_CREATE,
    NOW_ON_UPDATE,
    NOW_ON_CREATE_AND_UPDATE
  }

}
