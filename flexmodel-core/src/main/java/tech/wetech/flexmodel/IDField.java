package tech.wetech.flexmodel;

import java.io.Serializable;

import static tech.wetech.flexmodel.GeneratedValue.AUTO_INCREMENT;

/**
 * ID字段类型默认为字符串，当为自增时则为数字，为UUID时则为字符串
 *
 * @author cjbi
 */
public class IDField extends TypedField<Serializable, IDField> {

  public IDField(String name) {
    super(name, ScalarType.ID.getType());
    setNullable(false);
  }

  @Override
  public boolean isNullable() {
    return false;
  }

  public String getBaseType(){
    if(getDefaultValue() instanceof GeneratedValue generatedValue) {
      if(generatedValue.equals(AUTO_INCREMENT)) {
        return ScalarType.LONG.getType();
      } else {
        return ScalarType.STRING.getType();
      }
    } else {
      return ScalarType.STRING.getType();
    }
  }

}
