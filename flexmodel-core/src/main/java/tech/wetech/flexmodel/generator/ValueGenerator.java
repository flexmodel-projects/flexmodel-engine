package tech.wetech.flexmodel.generator;

import tech.wetech.flexmodel.TypedField;

import java.io.Serializable;
import java.util.Map;

/**
 * 值生成器
 *
 * @author cjbi
 */
public interface ValueGenerator<T> extends Serializable {

  GenerationTime getGenerationTime();

  T generateValue(TypedField<T, ?> field, Map<String, Object> data) throws ValueGenerationException;

  String getType();

}
