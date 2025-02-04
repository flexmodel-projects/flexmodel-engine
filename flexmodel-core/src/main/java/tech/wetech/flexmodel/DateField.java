package tech.wetech.flexmodel;

import java.time.LocalDate;

/**
 * @author cjbi
 */
public class DateField extends TypedField<LocalDate, DateField> {

  private GeneratedValue generatedValue;

  public DateField(String name) {
    super(name, ScalarType.DATE.getType());
  }

  public GeneratedValue getGeneratedValue() {
    return generatedValue;
  }

  public DateField setGeneratedValue(GeneratedValue generatedValue) {
    this.generatedValue = generatedValue;
    return this;
  }

  public enum GeneratedValue {
    NOW_ON_CREATE,
    NOW_ON_UPDATE,
    NOW_ON_CREATE_AND_UPDATE;
  }

}
