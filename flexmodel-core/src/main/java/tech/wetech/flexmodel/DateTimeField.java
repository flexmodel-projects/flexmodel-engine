package tech.wetech.flexmodel;

import java.time.LocalDateTime;

/**
 * @author cjbi
 */
public class DateTimeField extends TypedField<LocalDateTime, DateTimeField> {

  public DateTimeField(String name) {
    super(name, ScalarType.DATETIME.getType());
  }

}
