package tech.wetech.flexmodel;

import tech.wetech.flexmodel.calculations.DefaultValueCalculator;
import tech.wetech.flexmodel.calculations.ValueCalculator;
import tech.wetech.flexmodel.validations.ConstraintValidator;
import tech.wetech.flexmodel.validations.NotNullValidator;

import java.util.HashSet;
import java.util.Set;

/**
 * @author cjbi
 */
@SuppressWarnings("unchecked")
public class TypedField<T, SELF extends TypedField<T, SELF>> implements Field {
  private final String name;
  private final String type;
  private String modelName;
  private String comment;
  private boolean unique;
  private boolean nullable = true;
  private T defaultValue;
  private final Set<ConstraintValidator<T>> validators = new HashSet<>();
  private final Set<ValueCalculator<T>> calculators = new HashSet<>();

  public SELF setModelName(String modelName) {
    this.modelName = modelName;
    return self();
  }


  public TypedField(String name, String type) {
    this.name = name;
    this.type = type;
  }

  @Override
  public String name() {
    return name;
  }

  public String comment() {
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

  public T defaultValue() {
    return defaultValue;
  }

  public SELF setDefaultValue(T defaultValue) {
    this.defaultValue = defaultValue;
    addCalculation(new DefaultValueCalculator<>(defaultValue));
    return self();
  }

  public String type() {
    return type;
  }

  public String modelName() {
    return modelName;
  }

  public boolean isNullable() {
    return nullable;
  }

  @SuppressWarnings("unchecked")
  public SELF setNullable(boolean nullable) {
    this.nullable = nullable;
    if (!nullable) {
      addValidation(NotNullValidator.INSTANCE);
    } else {
      validators.remove(NotNullValidator.INSTANCE);
    }
    return self();
  }

  @Override
  public String toString() {
    return getClass().getName() + '(' + name() + ')';
  }

  public Set<ConstraintValidator<T>> validators() {
    return validators;
  }

  public SELF addValidation(ConstraintValidator<T> constraintValidator) {
    this.validators.add(constraintValidator);
    return self();
  }

  public Set<ValueCalculator<T>> calculators() {
    return calculators;
  }

  @SuppressWarnings("all")
  public SELF addCalculation(ValueCalculator<T> calculator) {
    this.calculators.add(calculator);
    return self();
  }

  @Override
  public boolean equals(Object obj) {
    if (this.name() != null && obj instanceof TypedField) {
      return this.name().equals(((TypedField<T, SELF>) obj).name());
    }
    return false;
  }

  private SELF self() {
    return (SELF) this;
  }

}
