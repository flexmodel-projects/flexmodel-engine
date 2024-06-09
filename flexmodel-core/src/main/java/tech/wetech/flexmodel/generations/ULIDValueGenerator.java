package tech.wetech.flexmodel.generations;

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
  protected String generateCheckedValue(TypedField<String, ?> field, Map<String, Object> data) throws ValueGenerateException {
    return ULID.random().toString();
  }

}
