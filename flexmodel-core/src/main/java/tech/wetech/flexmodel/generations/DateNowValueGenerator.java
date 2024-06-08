package tech.wetech.flexmodel.generations;

import tech.wetech.flexmodel.TypedField;

import java.time.LocalDate;
import java.util.Map;

/**
 * @author cjbi
 */
public class DateNowValueGenerator extends AbstractValueGenerator<LocalDate, DateNowValueGenerator> {

  public DateNowValueGenerator() {
  }

  @Override
  protected LocalDate generateCheckedValue(TypedField<LocalDate, ?> field, Map<String, Object> data) throws ValueGenerateException {
    if (data.get(field.getName()) instanceof LocalDate) {
      return (LocalDate) data.get(field.getName());
    }
    return LocalDate.now();
  }

}
