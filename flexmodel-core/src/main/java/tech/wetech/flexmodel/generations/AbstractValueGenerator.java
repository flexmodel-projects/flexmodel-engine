package tech.wetech.flexmodel.generations;

import tech.wetech.flexmodel.TypedField;

import java.util.Map;

/**
 * @author cjbi
 */
public abstract class AbstractValueGenerator<T, SELF extends ValueGenerator<T>> implements ValueGenerator<T> {

  private boolean skipIfNonNull;

  public boolean isSkipIfNonNull() {
    return skipIfNonNull;
  }

  public SELF setSkipIfNonNull(boolean skipIfNonNull) {
    this.skipIfNonNull = skipIfNonNull;
    return self();
  }

  @Override
  @SuppressWarnings("unchecked")
  public T generate(TypedField<T, ?> field, Map<String, Object> data) throws ValueGenerateException {
    Object result;
    if (isSkipIfNonNull() && (result = data.get(field.getName())) != null) {
      return (T) result;
    }
    return generateCheckedValue(field, data);
  }

  protected abstract T generateCheckedValue(TypedField<T, ?> field, Map<String, Object> data);

  @Override
  public boolean equals(Object obj) {
    return this.getClass().equals(obj.getClass());
  }

  @Override
  public String getType() {
    return this.getClass().getSimpleName();
  }

  @SuppressWarnings("unchecked")
  private SELF self() {
    return (SELF) this;
  }

}
