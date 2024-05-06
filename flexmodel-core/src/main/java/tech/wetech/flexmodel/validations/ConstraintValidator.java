package tech.wetech.flexmodel.validations;

import tech.wetech.flexmodel.TypedField;

import java.io.Serializable;

/**
 * @author cjbi
 */
public interface ConstraintValidator<T> extends Serializable {

  String getType();

  void validate(TypedField<T, ?> field, T value) throws ConstraintValidException;

}
