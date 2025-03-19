package tech.wetech.flexmodel;

import java.time.LocalTime;

/**
 * @author cjbi
 */
public class TimeField extends TypedField<LocalTime, TimeField> {

  private GeneratedValue generatedValue;

  public TimeField(String name) {
    super(name, ScalarType.TIME.getType());
  }

  public GeneratedValue getGeneratedValue() {
    return generatedValue;
  }

  public TimeField setGeneratedValue(GeneratedValue generatedValue) {
    this.generatedValue = generatedValue;
    return this;
  }

  public enum GeneratedValue {
    NOW_ON_CREATE,
    NOW_ON_UPDATE,
    NOW_ON_CREATE_AND_UPDATE;
  }

}
