package tech.wetech.flexmodel.generations;

/**
 * @author cjbi
 */
public abstract class AbstractValueGenerator<T> implements ValueGenerator<T> {

  @Override
  public boolean equals(Object obj) {
    return this.getClass().equals(obj.getClass());
  }

  @Override
  public String getType() {
    return this.getClass().getSimpleName();
  }
}
