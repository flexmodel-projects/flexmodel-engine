package tech.wetech.flexmodel.generator;

import tech.wetech.flexmodel.TypedField;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author cjbi
 */
public class DatetimeNowValueGenerator extends AbstractValueGenerator<LocalDateTime, DatetimeNowValueGenerator> {

  public DatetimeNowValueGenerator() {
  }

  @Override
  public LocalDateTime generateValue(TypedField<LocalDateTime, ?> field, Map<String, Object> data) throws ValueGenerationException {
    if (data.get(field.getName()) instanceof LocalDateTime) {
      return (LocalDateTime) data.get(field.getName());
    }
    return LocalDateTime.now();
  }

}
