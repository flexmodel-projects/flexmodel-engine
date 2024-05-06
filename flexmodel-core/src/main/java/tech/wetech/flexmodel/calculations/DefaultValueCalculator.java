package tech.wetech.flexmodel.calculations;

import tech.wetech.flexmodel.TypedField;

import java.util.Map;

/**
 * @author cjbi
 */
public class DefaultValueCalculator<T> extends AbstractValueCalculator<T> {

  private final T value;

  public DefaultValueCalculator(T value) {
    this.value = value;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T calculate(TypedField<T, ?> field, Map<String, Object> data) throws ValueCalculateException {
    T currentValue = (T) data.get(field.name());
    if (currentValue == null) {
      return value;
    }
    return currentValue;
  }

  public T getValue() {
    return value;
  }
}
