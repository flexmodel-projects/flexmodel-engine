package tech.wetech.flexmodel;

import tech.wetech.flexmodel.generator.FixedValueGenerator;
import tech.wetech.flexmodel.generator.ValueGenerator;
import tech.wetech.flexmodel.validator.ConstraintValidator;
import tech.wetech.flexmodel.validator.NotNullValidator;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author cjbi
 */
public class TypedField<T, SELF extends TypedField<T, SELF>> implements Field {
  private String name;
  private final String type;
  private String modelName;
  private String comment;
  private boolean unique;
  private boolean nullable = true;
  private T defaultValue;
  private final Set<ConstraintValidator<T>> validators = new HashSet<>();
  private ValueGenerator<T> generator;

  public TypedField(String name, String type) {
    this.name = name;
    this.type = type;
  }

  public SELF setModelName(String modelName) {
    this.modelName = modelName;
    return self();
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  public String getComment() {
    return comment;
  }

  public SELF setComment(String comment) {
    this.comment = comment;
    return self();
  }

  public boolean isUnique() {
    return unique;
  }

  public SELF setUnique(boolean unique) {
    this.unique = unique;
    return self();
  }

  public T getDefaultValue() {
    return defaultValue;
  }

  public SELF setDefaultValue(T defaultValue) {
    this.defaultValue = defaultValue;
    if (defaultValue != null) {
      this.generator = new FixedValueGenerator<>(defaultValue);
    }
    return self();
  }

  public String getType() {
    return type;
  }

  public String getModelName() {
    return modelName;
  }

  public boolean isNullable() {
    return nullable;
  }

  @SuppressWarnings("unchecked")
  public SELF setNullable(boolean nullable) {
    this.nullable = nullable;
    if (!nullable) {
      addValidator(NotNullValidator.INSTANCE);
    } else {
      validators.remove(NotNullValidator.INSTANCE);
    }
    return self();
  }

  @Override
  public String toString() {
    return getClass().getName() + '(' + getName() + ')';
  }

  public Set<ConstraintValidator<T>> getValidators() {
    return validators;
  }

  public SELF addValidator(ConstraintValidator<T> constraintValidator) {
    this.validators.add(constraintValidator);
    return self();
  }


  public ValueGenerator<T> getGenerator() {
    return generator;
  }

  @SuppressWarnings("all")
  public SELF setGenerator(ValueGenerator<T> generator) {
    if (generator instanceof FixedValueGenerator<T> fixedValueGenerator) {
      this.defaultValue = fixedValueGenerator.getValue();
    }
    this.generator = generator;
    return self();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TypedField<?, ?> that)) return false;

    if (unique != that.unique) return false;
    if (nullable != that.nullable) return false;
    if (!Objects.equals(name, that.name)) return false;
    if (!Objects.equals(type, that.type)) return false;
    if (!Objects.equals(modelName, that.modelName)) return false;
    if (!Objects.equals(comment, that.comment)) return false;
    if (!Objects.equals(defaultValue, that.defaultValue)) return false;
    if (!validators.equals(that.validators)) return false;
    return Objects.equals(generator, that.generator);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type, modelName, comment, unique, nullable, defaultValue, validators, generator);
  }

  //  @Override
//  @SuppressWarnings("unchecked")
//  public boolean equals(Object obj) {
//    if (this.getName() != null && obj instanceof TypedField) {
//      return this.getName().equals(((TypedField<T, SELF>) obj).getName());
//    }
//    return false;
//  }

  @SuppressWarnings("unchecked")
  private SELF self() {
    return (SELF) this;
  }

}
