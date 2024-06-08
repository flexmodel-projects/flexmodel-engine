package tech.wetech.flexmodel.generations;

import tech.wetech.flexmodel.TypedField;

import java.util.Map;
import java.util.UUID;

/**
 * 生成UUID
 *
 * @author cjbi
 */
public class UUIDValueGenerator extends AbstractValueGenerator<String> {

  public static final UUIDValueGenerator INSTANCE = new UUIDValueGenerator();

  @Override
  public String generate(TypedField<String, ?> field, Map<String, Object> data) throws ValueGenerateException {
    return UUID.randomUUID().toString();
  }

}
