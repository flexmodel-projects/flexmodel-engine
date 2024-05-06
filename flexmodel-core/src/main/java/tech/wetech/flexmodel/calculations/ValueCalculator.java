package tech.wetech.flexmodel.calculations;

import tech.wetech.flexmodel.TypedField;

import java.io.Serializable;
import java.util.Map;

/**
 * 值计算器
 *
 * @author cjbi
 */
public interface ValueCalculator<T> extends Serializable {

  T calculate(TypedField<T, ?> field, Map<String, Object> data) throws ValueCalculateException;

  String getType();

}
