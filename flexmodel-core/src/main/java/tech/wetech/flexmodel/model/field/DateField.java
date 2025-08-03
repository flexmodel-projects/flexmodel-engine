package tech.wetech.flexmodel.model.field;

import java.time.LocalDate;

/**
 * @author cjbi
 */
public class DateField extends TypedField<LocalDate, DateField> {

  public DateField(String name) {
    super(name, ScalarType.DATE.getType());
  }

}
