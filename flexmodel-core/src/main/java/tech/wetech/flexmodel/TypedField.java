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
  private String name;
  private final String type;
  private String modelName;
  private String comment;
  private boolean unique;
  private boolean nullable = true;
  private T defaultValue;
  private final Set<ConstraintValidator<T>> validators = new HashSet<>();
  private final Set<ValueCalculator<T>> calculators = new HashSet<>();

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
    addCalculation(new DefaultValueCalculator<>(defaultValue));
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
      addValidation(NotNullValidator.INSTANCE);
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

  public SELF addValidation(ConstraintValidator<T> constraintValidator) {
    this.validators.add(constraintValidator);
    return self();
  }

  public Set<ValueCalculator<T>> getCalculators() {
    return calculators;
  }

  @SuppressWarnings("all")
  public SELF addCalculation(ValueCalculator<T> calculator) {
    this.calculators.add(calculator);
    return self();
  }

  @Override
  public boolean equals(Object obj) {
    if (this.getName() != null && obj instanceof TypedField) {
      return this.getName().equals(((TypedField<T, SELF>) obj).getName());
    }
    return false;
  }

  private SELF self() {
    return (SELF) this;
  }

}
