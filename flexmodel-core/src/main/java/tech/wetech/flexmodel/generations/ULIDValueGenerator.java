package tech.wetech.flexmodel.generations;

import tech.wetech.flexmodel.TypedField;
import tech.wetech.flexmodel.ULID;

import java.util.Map;

/**
 * 生成UUID
 *
 * @author cjbi
 */
public class ULIDValueGenerator extends AbstractValueGenerator<String> {

  public static final ULIDValueGenerator INSTANCE = new ULIDValueGenerator();

  @Override
  public String generate(TypedField<String, ?> field, Map<String, Object> data) throws ValueGenerateException {
    return ULID.random().toString();
  }

}
