package tech.wetech.flexmodel.calculations;

import tech.wetech.flexmodel.TypedField;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author cjbi
 */
public class DatetimeNowValueCalculator extends AbstractValueCalculator<LocalDateTime> {

  private boolean alwaysCalc = true;

  public DatetimeNowValueCalculator() {
  }

  public DatetimeNowValueCalculator(boolean alwaysCalc) {
    this.alwaysCalc = alwaysCalc;
  }

  @Override
  public LocalDateTime calculate(TypedField<LocalDateTime, ?> field, Map<String, Object> data) throws ValueCalculateException {
    if (!alwaysCalc && data.get(field.getName()) instanceof LocalDateTime) {
      return (LocalDateTime) data.get(field.getName());
    }
    return LocalDateTime.now();
  }

}
