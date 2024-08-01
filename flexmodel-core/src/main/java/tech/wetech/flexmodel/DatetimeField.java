package tech.wetech.flexmodel;

import java.time.LocalDateTime;

/**
 * @author cjbi
 */
public class DatetimeField extends TypedField<LocalDateTime, DatetimeField> {

  public DatetimeField(String name) {
    super(name, ScalarType.DATETIME.getType());
  }

}
