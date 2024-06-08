package tech.wetech.flexmodel.generations;

import tech.wetech.flexmodel.TypedField;

import java.time.LocalDate;
import java.util.Map;

/**
 * @author cjbi
 */
public class DateNowValueGenerator extends AbstractValueGenerator<LocalDate> {

  public DateNowValueGenerator() {
  }

  @Override
  public LocalDate generate(TypedField<LocalDate, ?> field, Map<String, Object> data) throws ValueGenerateException {
    if (data.get(field.getName()) instanceof LocalDate) {
      return (LocalDate) data.get(field.getName());
    }
    return LocalDate.now();
  }

}
