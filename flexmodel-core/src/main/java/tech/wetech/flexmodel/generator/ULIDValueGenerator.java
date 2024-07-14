package tech.wetech.flexmodel.generator;

import tech.wetech.flexmodel.TypedField;

import java.util.Map;

/**
 * 生成UUID
 *
 * @author cjbi
 */
public class ULIDValueGenerator extends AbstractValueGenerator<String, ULIDValueGenerator> {

  public static final ULIDValueGenerator INSTANCE = new ULIDValueGenerator();

  @Override
  public String generateValue(TypedField<String, ?> field, Map<String, Object> data) throws ValueGenerationException {
    if (data.get(field.getName()) instanceof String str) {
      return str;
    }
    return ULID.random().toString();
  }

}
