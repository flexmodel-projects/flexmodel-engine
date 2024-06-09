package tech.wetech.flexmodel.generations;

import tech.wetech.flexmodel.TypedField;

import java.util.Map;

/**
 * @author cjbi
 */
public class FixedValueGenerator<T> extends AbstractValueGenerator<T, FixedValueGenerator<T>> {

  private final T value;

  public FixedValueGenerator(T value) {
    this.value = value;
  }

  @Override
  @SuppressWarnings("unchecked")
  protected T generateCheckedValue(TypedField<T, ?> field, Map<String, Object> data) throws ValueGenerateException {
    T currentValue = (T) data.get(field.getName());
    if (currentValue == null) {
      return value;
    }
    return currentValue;
  }

  public T getValue() {
    return value;
  }
}
