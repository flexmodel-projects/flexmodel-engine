package tech.wetech.flexmodel.calculations;

import tech.wetech.flexmodel.TypedField;
import tech.wetech.flexmodel.ULID;

import java.util.Map;

/**
 * 生成UUID
 *
 * @author cjbi
 */
public class ULIDValueCalculator extends AbstractValueCalculator<String> {

  public static final ULIDValueCalculator INSTANCE = new ULIDValueCalculator();

  @Override
  public String calculate(TypedField<String, ?> field, Map<String, Object> data) throws ValueCalculateException {
    return ULID.random().toString();
  }

}
