package tech.wetech.flexmodel.calculations;

import tech.wetech.flexmodel.TypedField;

import java.time.LocalDate;
import java.util.Map;

/**
 * @author cjbi
 */
public class DateNowValueCalculator extends AbstractValueCalculator<LocalDate> {

  private boolean alwaysCalc = true;

  public DateNowValueCalculator() {
  }

  public DateNowValueCalculator(boolean alwaysCalc) {
    this.alwaysCalc = alwaysCalc;
  }

  @Override
  public LocalDate calculate(TypedField<LocalDate, ?> field, Map<String, Object> data) throws ValueCalculateException {
    if (!alwaysCalc && data.get(field.getName()) instanceof LocalDate) {
      return (LocalDate) data.get(field.getName());
    }
    return LocalDate.now();
  }

}
