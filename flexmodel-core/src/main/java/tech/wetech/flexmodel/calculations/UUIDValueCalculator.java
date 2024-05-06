package tech.wetech.flexmodel.calculations;

import tech.wetech.flexmodel.TypedField;

import java.util.Map;
import java.util.UUID;

/**
 * 生成UUID
 *
 * @author cjbi
 */
public class UUIDValueCalculator extends AbstractValueCalculator<String> {

  public static final UUIDValueCalculator INSTANCE = new UUIDValueCalculator();

  @Override
  public String calculate(TypedField<String, ?> field, Map<String, Object> data) throws ValueCalculateException {
    return UUID.randomUUID().toString();
  }

}
