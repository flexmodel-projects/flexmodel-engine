package tech.wetech.flexmodel.generator;

/**
 * @author cjbi
 */
public abstract class AbstractValueGenerator<T, SELF extends ValueGenerator<T>> implements ValueGenerator<T> {

  protected GenerationTime generationTime = GenerationTime.ALWAYS;

  @Override
  public GenerationTime getGenerationTime() {
    return generationTime;
  }

  public SELF setGenerationTime(GenerationTime generationTime) {
    this.generationTime = generationTime;
    return self();
  }

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
