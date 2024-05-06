package tech.wetech.flexmodel.calculations;

/**
 * @author cjbi
 */
public abstract class AbstractValueCalculator<T> implements ValueCalculator<T> {

  @Override
  public boolean equals(Object obj) {
    return this.getClass().equals(obj.getClass());
  }

  @Override
  public String getType() {
    return this.getClass().getSimpleName();
  }
}
