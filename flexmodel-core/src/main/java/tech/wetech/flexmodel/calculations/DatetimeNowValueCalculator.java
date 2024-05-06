package tech.wetech.flexmodel.calculations;

import tech.wetech.flexmodel.TypedField;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author cjbi
 */
public class DatetimeNowValueCalculator extends AbstractValueCalculator<LocalDateTime> {

  @Override
  public LocalDateTime calculate(TypedField<LocalDateTime, ?> field, Map<String, Object> data) throws ValueCalculateException {
    return LocalDateTime.now();
  }

}
