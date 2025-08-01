package tech.wetech.flexmodel.core.model.field;

import java.time.LocalTime;

/**
 * @author cjbi
 */
public class TimeField extends TypedField<LocalTime, TimeField> {

  public TimeField(String name) {
    super(name, ScalarType.TIME.getType());
  }

}
