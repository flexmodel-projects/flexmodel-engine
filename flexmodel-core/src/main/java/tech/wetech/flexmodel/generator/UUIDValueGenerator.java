package tech.wetech.flexmodel.generator;

import tech.wetech.flexmodel.TypedField;

import java.util.Map;
import java.util.UUID;

/**
 * 生成UUID
 *
 * @author cjbi
 */
public class UUIDValueGenerator extends AbstractValueGenerator<String, UUIDValueGenerator> {

  public static final UUIDValueGenerator INSTANCE = new UUIDValueGenerator();

  @Override
  public String generateValue(TypedField<String, ?> field, Map<String, Object> data) throws ValueGenerationException {
    if (data.get(field.getName()) instanceof String str) {
      return str;
    }
    return UUID.randomUUID().toString();
  }

}
