package tech.wetech.flexmodel.generations;

import tech.wetech.flexmodel.TypedField;

import java.util.Map;

/**
 * @author cjbi
 */
public class DefaultValueGenerator<T> extends AbstractValueGenerator<T> {

  private final T value;

  public DefaultValueGenerator(T value) {
    this.value = value;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T generate(TypedField<T, ?> field, Map<String, Object> data) throws ValueGenerateException {
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
