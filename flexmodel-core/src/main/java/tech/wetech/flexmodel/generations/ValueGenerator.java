package tech.wetech.flexmodel.generations;

import tech.wetech.flexmodel.TypedField;

import java.io.Serializable;
import java.util.Map;

/**
 * 值计算器
 *
 * @author cjbi
 */
public interface ValueGenerator<T> extends Serializable {

  T generate(TypedField<T, ?> field, Map<String, Object> data) throws ValueGenerateException;

  String getType();

}
