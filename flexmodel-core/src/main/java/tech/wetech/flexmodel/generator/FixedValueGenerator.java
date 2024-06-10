package tech.wetech.flexmodel.generator;

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
  public T generateValue(TypedField<T, ?> field, Map<String, Object> data) throws ValueGenerationException {
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
