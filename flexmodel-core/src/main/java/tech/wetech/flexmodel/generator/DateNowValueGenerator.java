package tech.wetech.flexmodel.generator;

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
  public LocalDate generateValue(TypedField<LocalDate, ?> field, Map<String, Object> data) throws ValueGenerationException {
    if (data.get(field.getName()) instanceof LocalDate) {
      return (LocalDate) data.get(field.getName());
    }
    return LocalDate.now();
  }

}
