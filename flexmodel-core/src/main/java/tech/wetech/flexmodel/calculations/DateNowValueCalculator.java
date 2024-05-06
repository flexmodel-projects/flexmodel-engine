package tech.wetech.flexmodel.calculations;

import tech.wetech.flexmodel.TypedField;

import java.time.LocalDate;
import java.util.Map;

/**
 * @author cjbi
 */
public class DateNowValueCalculator extends AbstractValueCalculator<LocalDate> {

  @Override
  public LocalDate calculate(TypedField<LocalDate, ?> field, Map<String, Object> data) throws ValueCalculateException {
    return LocalDate.now();
  }
}
