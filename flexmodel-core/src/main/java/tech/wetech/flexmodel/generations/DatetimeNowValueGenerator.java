package tech.wetech.flexmodel.generations;

import tech.wetech.flexmodel.TypedField;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author cjbi
 */
public class DatetimeNowValueGenerator extends AbstractValueGenerator<LocalDateTime> {

  public DatetimeNowValueGenerator() {
  }

  @Override
  public LocalDateTime generate(TypedField<LocalDateTime, ?> field, Map<String, Object> data) throws ValueGenerateException {
    if (data.get(field.getName()) instanceof LocalDateTime) {
      return (LocalDateTime) data.get(field.getName());
    }
    return LocalDateTime.now();
  }

}
